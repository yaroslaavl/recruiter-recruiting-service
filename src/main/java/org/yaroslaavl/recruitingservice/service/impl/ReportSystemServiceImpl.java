package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.broker.RecruitingAppNotificationEventPublisher;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.database.repository.ReportSystemRepository;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ReportSystemShortDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyAlreadyReportedInfo;
import org.yaroslaavl.recruitingservice.dto.response.list.UserReportsShortDto;
import org.yaroslaavl.recruitingservice.exception.*;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemLimitInfo;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.mapper.ReportSystemMapper;
import org.yaroslaavl.recruitingservice.service.ReportSystemService;
import org.yaroslaavl.recruitingservice.util.NotificationStore;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSystemServiceImpl implements ReportSystemService {

    @Value("${vacancy.max_reports_by_user_within_time_span}")
    private Long maxReportCountByUserIdWithinTimeSpan;

    private final ReportSystemMapper reportSystemMapper;
    private final ReportSystemRepository reportSystemRepository;
    private final UserFeignClient userFeignClient;
    private final SecurityContextServiceImpl securityContextService;
    private final VacancyRepository vacancyRepository;
    private final RecruitingAppNotificationEventPublisher publisher;
    private static final Integer REPORT_TIME_SPAN_DAYS = 7;

    /**
     * Processes a report submitted by a user for a specific vacancy. Validates the user's approval status,
     * ensures the vacancy is active, checks if the user has already reported the vacancy, and enforces a maximum
     * limit on the number of reports a user can submit within a defined time span. If all validations are passed,
     * the report is created and stored in the report system.
     *
     * @param reportRequestDto the data transfer object containing the details of the report being submitted, such as
     *                         the vacancy ID, report reason, and optional comment.
     * @throws VacancyIsNotActiveException if the vacancy associated with the report is not currently active.
     * @throws VacancyAlreadyReportedException if the user has previously reported the same vacancy and the status of the report is still new.
     * @throws MaxReportsPerTimeSpanException if the user has already reached the maximum allowable number of reports within the defined time span.
     */
    @Override
    @Transactional
    public void report(ReportRequestDto reportRequestDto) {
        String userId = securityContextService.getSecurityContext(Credentials.SUB);
        boolean approved
                = userFeignClient.isApproved(userId);

        if (approved) {
            Vacancy vacancy = getVacancy(reportRequestDto.vacancyId());

            if (vacancy.getStatus() != VacancyStatus.ENABLED) {
                log.info("Vacancy with id '{}' is not enabled", vacancy.getId());
                throw new VacancyIsNotActiveException("Vacancy not found with id: " + reportRequestDto.vacancyId());
            }
            boolean isAlreadyReportedByUser = reportSystemRepository.existsReportSystemByUserIdAndVacancy_IdAndStatus_New(userId, vacancy.getId());

            if (isAlreadyReportedByUser) {
                log.info("User with id '{}' already reported vacancy with id '{}'", userId, vacancy.getId());
                throw new VacancyAlreadyReportedException("User already reported vacancy", informAboutReportedVacancy(userId, vacancy.getId()));
            }

            LocalDateTime timeSpan = LocalDateTime.now().minusDays(REPORT_TIME_SPAN_DAYS);
            long quantityOfReports = reportSystemRepository.countReportSystemByUserIdWithinTimeSpan(userId, timeSpan);

            if (quantityOfReports >= maxReportCountByUserIdWithinTimeSpan) {
                log.info("User with id '{}' has already reported {} vacancies within last {} days", userId, maxReportCountByUserIdWithinTimeSpan, REPORT_TIME_SPAN_DAYS);

                throw new MaxReportsPerTimeSpanException("User has reached maximum number of reports",
                        informAboutReportLimit(userId, timeSpan));
            }

            ReportSystem reportSystem = ReportSystem.builder()
                    .vacancy(vacancy)
                    .userId(userId)
                    .status(RecruitingSystemStatus.NEW)
                    .reportReason(reportRequestDto.vacancyReportReason())
                    .comment(reportRequestDto.comment())
                    .build();

            reportSystemRepository.save(reportSystem);
            publisher.publishInAppNotification(NotificationStore.inAppNotification(null, vacancy.getRecruiterId(), String.valueOf(vacancy.getId()), "VACANCY_REPORTED", Map.of("vacancyTitle", vacancy.getTitle(), "reason", reportRequestDto.vacancyReportReason().name())));
        } else {
            log.info("User with id '{}' is not approved", userId);
            throw new UserAccountApprovedException("User is not approved");
        }
    }

    /**
     * Resolves a report by updating its status in the system. Ensures that the new status is different
     * from the current status to prevent redundant updates. If the report with the given identifier
     * is not found, an exception is thrown.
     *
     * @param reportId the unique identifier of the report to be resolved.
     * @param newStatus the new status to be assigned to the report. Must be different
     *                  from the current status of the report to proceed with the update.
     * @throws EntityNotFoundException if no report is found with the specified {@code reportId}.
     * @throws SameReportStatusException if the current report status is identical to {@code newStatus}.
     */
    @Override
    public void resolveReport(UUID reportId, RecruitingSystemStatus newStatus) {
        ReportSystem reportSystem = reportSystemRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + reportId));

        if (reportSystem.getStatus() == newStatus) {
            throw new SameReportStatusException("Cannot change report status from " + reportSystem.getStatus() + " to " + newStatus);
        }

        reportSystem.setStatus(newStatus);
        reportSystemRepository.save(reportSystem);
    }

    /**
     * Retrieves a paginated list of filtered report summaries for a given status.
     *
     * @param status the filtering status used to retrieve reports (e.g., NEW, RESOLVED, etc.)
     * @param pageable an object specifying pagination and sorting information
     * @return a paginated list of {@link ReportSystemShortDto} representing the filtered reports
     */
    @Override
    public PageShortDto<ReportSystemShortDto> getFilteredReports(RecruitingSystemStatus status, Pageable pageable) {
        log.info("Getting filtered reports with status {}", status);

        Page<ReportSystem> reportSystemsByFilteredStatus
                = reportSystemRepository.findReportSystemsByFilteredStatus(status, pageable);

        Set<String> userIds = reportSystemsByFilteredStatus.stream().map(ReportSystem::getUserId).collect(Collectors.toSet());

        Map<String, String> usersDisplayName = userFeignClient.usersDisplayName(userIds, securityContextService.getSecurityContext(Credentials.EMAIL));

        if (usersDisplayName == null) {
            usersDisplayName = Map.of();
        }

        log.info("Found {} filtered reports", reportSystemsByFilteredStatus.getTotalElements());
        return new PageShortDto<>(
                reportSystemMapper.toShortDto(reportSystemsByFilteredStatus.getContent(), usersDisplayName),
                reportSystemsByFilteredStatus.getTotalElements(),
                reportSystemsByFilteredStatus.getTotalPages(),
                reportSystemsByFilteredStatus.getNumber(),
                reportSystemsByFilteredStatus.getSize());
    }

    @Override
    public ReportSystemResponseDto getReport(UUID id) {
        ReportSystem reportSystem = reportSystemRepository.findByIdWithVacancy(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found with id: " + id));

        return reportSystemMapper.toResponseDto(reportSystem);
    }

    @Override
    public PageShortDto<UserReportsShortDto> getMyReports(Pageable pageable) {
        Page<ReportSystem> reports =
                reportSystemRepository.findReportSystemsByUserId(securityContextService.getSecurityContext(Credentials.SUB), pageable);

        if (reports.getContent().isEmpty()) {
            new PageShortDto<>(Collections.emptyList(), 0, 0, 0, 0);
        }

        return new PageShortDto<>(
                reportSystemMapper.toUserShortDto(reports.getContent()),
                reports.getTotalPages(),
                reports.getTotalElements(),
                reports.getNumber(),
                reports.getSize(
        ));
    }

    private Vacancy getVacancy(UUID vacancyId) {
        return vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new EntityNotFoundException("Vacancy not found with id: " + vacancyId));
    }

    private ReportSystemLimitInfo informAboutReportLimit(String userId, LocalDateTime timeSpan) {
        Optional<ReportSystem> firstReport
                = reportSystemRepository.findFirstByUserIdAndCreatedAtAfterOrderByCreatedAtAsc(userId, timeSpan);

        LocalDateTime firstReportDateTime = Objects.requireNonNull(firstReport.map(ReportSystem::getCreatedAt).orElse(null)).plusDays(REPORT_TIME_SPAN_DAYS);

        return new ReportSystemLimitInfo(maxReportCountByUserIdWithinTimeSpan, firstReportDateTime);
    }

    private VacancyAlreadyReportedInfo informAboutReportedVacancy(String userId, UUID vacancyId) {
        Optional<ReportSystem> report
                = reportSystemRepository.findReportSystemsByUserIdAndVacancy_Id(userId, vacancyId);

        return report.map(reportSystem -> new VacancyAlreadyReportedInfo(reportSystem.getReportReason(), reportSystem.getCreatedAt())).orElse(null);
    }
}
