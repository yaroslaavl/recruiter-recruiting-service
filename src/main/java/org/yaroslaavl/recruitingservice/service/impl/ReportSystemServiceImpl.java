package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.database.entity.ReportSystem;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.database.repository.ReportSystemRepository;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyAlreadyReportedInfo;
import org.yaroslaavl.recruitingservice.exception.MaxReportsPerTimeSpanException;
import org.yaroslaavl.recruitingservice.exception.SameReportStatusException;
import org.yaroslaavl.recruitingservice.exception.VacancyAlreadyReportedException;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemLimitInfo;
import org.yaroslaavl.recruitingservice.exception.VacancyIsNotActiveException;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.mapper.ReportSystemMapper;
import org.yaroslaavl.recruitingservice.service.ReportSystemService;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
    private static final Integer REPORT_TIME_SPAN_DAYS = 7;

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
            //send notification to Recruiter and candidate
        }
    }

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

    @Override
    public Page<ReportSystemResponseDto> getReports(UUID vacancyId, Pageable pageable) {
        Vacancy vacancy = getVacancy(vacancyId);

        Page<ReportSystem> reportSystemsByFilteredStatus
                = reportSystemRepository.findReportSystemsByFilteredStatus(vacancy.getId(), pageable);

        return reportSystemsByFilteredStatus.map(reportSystemMapper::toResponseDto);
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
