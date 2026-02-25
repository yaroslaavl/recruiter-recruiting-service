package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.yaroslaavl.recruitingservice.broker.RecruitingAppNotificationEventPublisher;
import org.yaroslaavl.recruitingservice.database.entity.Application;
import org.yaroslaavl.recruitingservice.database.entity.ApplicationHistory;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.Credentials;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationHistoryRepository;
import org.yaroslaavl.recruitingservice.database.repository.ApplicationRepository;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ApplicationShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.CandidateApplicationsShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.exception.*;
import org.yaroslaavl.recruitingservice.feignClient.cv.CvFeignClient;
import org.yaroslaavl.recruitingservice.feignClient.dto.ApplicationChatInfo;
import org.yaroslaavl.recruitingservice.feignClient.dto.CVApplicationDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.CompanyPreviewFeignDto;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.feignClient.dto.UserFeignDto;
import org.yaroslaavl.recruitingservice.mapper.ApplicationMapper;
import org.yaroslaavl.recruitingservice.service.ApplicationService;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;
import org.yaroslaavl.recruitingservice.util.NotificationStore;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final CvFeignClient cvFeignClient;
    private final UserFeignClient userFeignClient;
    private final ApplicationRepository applicationRepository;
    private final SecurityContextService securityContextService;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final VacancyRepository vacancyRepository;
    private final ApplicationMapper applicationMapper;
    private final RecruitingAppNotificationEventPublisher publisher;

    private static final Map<RecruitingSystemStatus, EnumSet<RecruitingSystemStatus>> ALLOWED_STATUSES = Map.of(
            RecruitingSystemStatus.NEW, EnumSet.noneOf(RecruitingSystemStatus.class),
            RecruitingSystemStatus.VIEWED, EnumSet.of(
                    RecruitingSystemStatus.IN_PROGRESS,
                    RecruitingSystemStatus.REJECTED,
                    RecruitingSystemStatus.NO_MORE_INTERESTS),
            RecruitingSystemStatus.IN_PROGRESS, EnumSet.of(
                    RecruitingSystemStatus.ACCEPTED,
                    RecruitingSystemStatus.REJECTED,
                    RecruitingSystemStatus.NO_MORE_INTERESTS)
    );

    /**
     * Applies for a recruiting based on the provided recruiting application request.
     * The method validates the candidate's approval status, retrieves the CV for the recruiter,
     * checks for any existing application for the given recruiting, and either updates the application
     * or creates a new one based on the current status. Throws exceptions if the candidate is not
     * approved, the CV is not available, or if there are blocking conditions due to existing applications.
     *
     * @param vacancyApplicationRequestDto the data transfer object containing information about the vacancy
     *                                     application, including the vacancy ID, CV ID, and cover letter
     */
    @Override
    @Transactional
    public void applyVacancy(VacancyApplicationRequestDto vacancyApplicationRequestDto) {
        log.info("Applying vacancy: {}", vacancyApplicationRequestDto.vacancyId());

        String candidateKeyId = securityContextService.getSecurityContext(Credentials.SUB);
        boolean approved = userFeignClient.isApproved(candidateKeyId);

        if (!approved) {
            throw new CandidateAccountStatusException("User is not approved");
        }

        CVApplicationDto cvForRecruiter = cvFeignClient.getCvForRecruiter(vacancyApplicationRequestDto.cvId());

        if (cvForRecruiter == null) {
            throw new GetCvException("Cv is not found or not readable");
        }

        Vacancy vacancy = vacancyRepository.findById(vacancyApplicationRequestDto.vacancyId()).orElseThrow(
                () -> new EntityNotFoundException("Vacancy with id: " + vacancyApplicationRequestDto.vacancyId() + " not found"));

        if (vacancy.getStatus() == VacancyStatus.ARCHIVED) {
            throw new RuntimeException("Vacancy is archived");
        }

        Optional<Application> optionalApplication = applicationRepository.findByVacancyIdAndCandidateId(
                vacancyApplicationRequestDto.vacancyId(),
                candidateKeyId);

        UUID applicationSendId = null;

        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();

            if (application.getStatus() == RecruitingSystemStatus.NO_MORE_INTERESTS) {
                log.info("Vacancy with id '{}' is no more interested in user with id '{}'",
                        vacancyApplicationRequestDto.vacancyId(),
                        candidateKeyId);

                throw new CandidateApplicationBlockedException("Company is no more interested in candidate with id: " + candidateKeyId);
            }

            if (application.getStatus() == RecruitingSystemStatus.VIEWED ||
                application.getStatus() == RecruitingSystemStatus.IN_PROGRESS) {
                log.info("Vacancy with id '{}' is already applied by user with id '{}'",
                        vacancyApplicationRequestDto.vacancyId(),
                        candidateKeyId);

                throw new CandidateAlreadyAppliedException("Candidate is already applied to vacancy");
            }

            if (application.getStatus() == RecruitingSystemStatus.REJECTED ||
                application.getStatus() == RecruitingSystemStatus.NEW) {

                application.setCvId(vacancyApplicationRequestDto.cvId());
                application.setCoverLetter(vacancyApplicationRequestDto.coverLetter());
                application.setStatus(RecruitingSystemStatus.NEW);
                application.setAppliedAt(LocalDateTime.now());

                applicationRepository.save(application);
                applicationSendId =  application.getId();
                applicationHistoryChanger(application, RecruitingSystemStatus.NEW, true);
            }
        } else {
            Application newApplication = Application.builder()
                    .vacancy(vacancy)
                    .cvId(vacancyApplicationRequestDto.cvId())
                    .candidateId(candidateKeyId)
                    .status(RecruitingSystemStatus.NEW)
                    .coverLetter(vacancyApplicationRequestDto.coverLetter())
                    .appliedAt(LocalDateTime.now())
                    .build();

            applicationRepository.save(newApplication);
            applicationSendId =  newApplication.getId();
            applicationHistoryChanger(newApplication, RecruitingSystemStatus.NEW, true);
        }

        publisher.publishInAppNotification(NotificationStore.inAppNotification(null, candidateKeyId, String.valueOf(applicationSendId), "APPLICATION_SUBMITTED", Map.of("vacancyTitle", vacancy.getTitle(), "submittedAt",  LocalDateTime.now().toString())));
    }

    /**
     * Retrieves a paginated and filtered list of application responses for a given vacancy and status.
     *
     * @param vacancyId the unique identifier of the vacancy for which applications are being retrieved
     * @param status the status filter to apply when retrieving the applications
     * @param pageable the pagination and sorting information
     * @return a page of application response DTOs that match the given vacancyId and status
     * @throws EntityNotFoundException if no vacancy with the specified vacancyId exists
     * @throws RecruiterNotBelongToCompanyOrVacancyException if the authenticated recruiter does not
     *         have access to the specified vacancy
     */
    @Override
    public PageShortDto<ApplicationShortDto> getFilteredApplications(UUID vacancyId,
                                                                     RecruitingSystemStatus status,
                                                                     String salary,
                                                                     String workMode,
                                                                     Integer availableHoursPerWeek,
                                                                     String availableFrom,
                                                                     Pageable pageable) {
        String recruiterKeyId = securityContextService.getSecurityContext(Credentials.SUB);

        Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                () -> new EntityNotFoundException("Vacancy with id: " + vacancyId + " not found"));

        if (!recruiterKeyId.equals(vacancy.getRecruiterId())) {
            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to vacancy");
        }

        Map<String, UserFeignDto> filteredCandidates = Optional.ofNullable(
                        userFeignClient.getFilteredCandidates(salary, workMode, availableHoursPerWeek, availableFrom))
                .orElse(Collections.emptyMap());

        List<@NotBlank String> userFilteredIds = filteredCandidates.keySet().stream().toList();

        Page<Application> applicationsByVacancyIdAndStatus = applicationRepository.findApplicationsByVacancyIdAndStatus(vacancyId, status, userFilteredIds, pageable);

        if (applicationsByVacancyIdAndStatus.isEmpty()) {
            log.info("No applications found for recruiting with id: {}", vacancyId);
            return new PageShortDto<>(Collections.emptyList(), 0, 0, 0, 0);
        }
        return new PageShortDto<>(
                applicationMapper.toShortDto(applicationsByVacancyIdAndStatus.getContent(), filteredCandidates),
                applicationsByVacancyIdAndStatus.getTotalElements(),
                applicationsByVacancyIdAndStatus.getTotalPages(),
                applicationsByVacancyIdAndStatus.getNumber(),
                applicationsByVacancyIdAndStatus.getSize());
    }

    /**
     * Retrieves the details of an application by its unique identifier.
     * If the application status is NEW, it updates the status to VIEWED,
     * logs the change in application history, and persists the updated application.
     *
     * @param applicationId the unique identifier of the application to be retrieved
     * @return an ApplicationDetailsResponseDto containing the details of the application
     *         mapped from the application entity
     */
    @Override
    @Transactional
    public ApplicationDetailsResponseDto getApplicationDetails(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Application with id: " + applicationId + " not found"
                ));

        String userId = securityContextService.getSecurityContext(Credentials.SUB);
        if (application.getVacancy().getRecruiterId().equals(userId)
                && application.getStatus() == RecruitingSystemStatus.NEW) {

            application.setStatus(RecruitingSystemStatus.VIEWED);

            applicationHistoryChanger(
                    application,
                    RecruitingSystemStatus.VIEWED,
                    Boolean.FALSE
            );

            applicationRepository.save(application);
        }

        return applicationMapper.toApplicationDetailsDto(application);
    }


    /**
     * Changes the status of an application to the specified new status. Validates the current state of the application
     * and ensures that the status transition is allowed based on predefined rules. Additionally, updates the associated
     * vacancy status and history if applicable and persists the changes.
     *
     * @param applicationId the unique identifier of the application whose status is to be changed
     * @param newStatus the new status to which the application is to be transitioned
     * @throws ViewApplicationException if the current application status is NEW and the recruiter is not allowed
     *         to change the status
     * @throws IllegalStateException if the transition from the current status to the specified new status is not allowed
     */
    @Override
    @Transactional
    public void changeApplicationStatus(UUID applicationId, RecruitingSystemStatus newStatus) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id: " + applicationId + " not found"));

        if (application.getStatus() == RecruitingSystemStatus.NEW) {
            throw new ViewApplicationException("Recruiter can't change status of application");
        }

        EnumSet<RecruitingSystemStatus> allowed = ALLOWED_STATUSES.getOrDefault(application.getStatus(), EnumSet.noneOf(RecruitingSystemStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Cannot change application from " + application.getStatus() + " to " + newStatus);
        }

        application.setStatus(newStatus);
        applicationHistoryChanger(application, newStatus, false);

        boolean isApproved = newStatus == RecruitingSystemStatus.ACCEPTED;
        if (isApproved) {
            application.getVacancy().setStatus(VacancyStatus.ARCHIVED);
            vacancyRepository.save(application.getVacancy());
            publisher.publishInAppNotification(NotificationStore.inAppNotification(null, application.getCandidateId(), String.valueOf(application.getId()), "APPLICATION_APPROVED",
                    Map.of("vacancyTitle", application.getVacancy().getTitle(),
                            "approvedAt", LocalDateTime.now().toString())));
        }

        if (!isApproved) {
            publisher.publishInAppNotification(NotificationStore.inAppNotification(null, application.getCandidateId(), String.valueOf(application.getId()), "APPLICATION_STATUS_CHANGED",
                    Map.of("vacancyTitle", application.getVacancy().getTitle(),
                            "oldStatus", application.getStatus().toString(),
                            "newStatus", newStatus.toString(),
                            "changedAt", LocalDateTime.now().toString())));
        }
        applicationRepository.save(application);
    }

    @Override
    public PageShortDto<CandidateApplicationsShortDto> getMyApplications(Pageable pageable) {
        Page<Application> applications
                = applicationRepository.findApplicationsByCandidateId(securityContextService.getSecurityContext(Credentials.SUB), pageable);

        Map<UUID, CompanyPreviewFeignDto> companyPreview =
                userFeignClient.previewInfo(applications.getContent().stream()
                        .map(Application::getVacancy)
                        .map(Vacancy::getCompanyId)
                        .collect(Collectors.toSet()));

        if (applications.getContent().isEmpty()) {
            return new PageShortDto<>(Collections.emptyList(), 0, 0, 0, 0);
        }

        return new PageShortDto<>(
                applicationMapper.toCandidateShortDto(applications.getContent(),  companyPreview),
                applications.getTotalElements(),
                applications.getTotalPages(),
                applications.getNumber(),
                applications.getSize()
        );
    }

    @Override
    public boolean isOpenedForChatting(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application with id: " + applicationId + " not found"));
       return application.getStatus() == RecruitingSystemStatus.IN_PROGRESS;
    }

    @Override
    public List<ApplicationChatInfo> getPreviewApplicationInfo(Set<UUID> applicationIds) {
        List<Application> applications = applicationRepository.findAllByApplicationsIds(applicationIds);

        Set<UUID> companyIds = applications.stream()
                .map(Application::getVacancy)
                .map(Vacancy::getCompanyId)
                .collect(Collectors.toSet());

        Map<UUID, CompanyPreviewFeignDto> companiesInfo = userFeignClient.previewInfo(companyIds);
        return applicationMapper.toApplicationChatInfo(applications, companiesInfo);
    }

    private void applicationHistoryChanger(Application application,
                                           RecruitingSystemStatus status, boolean isInitialApplication) {

        RecruitingSystemStatus oldStatus = applicationHistoryRepository
                .findTopByApplicationIdOrderByChangedAtDesc(application.getId())
                .map(ApplicationHistory::getNewStatus)
                .orElse(RecruitingSystemStatus.NEW);

        ApplicationHistory applicationHistory = new ApplicationHistory();
        applicationHistory.setApplication(application);
        applicationHistory.setOldStatus(oldStatus);
        applicationHistory.setNewStatus(status);
        applicationHistory.setChangedAt(LocalDateTime.now());

        String changedBy = isInitialApplication ? "system" : securityContextService.getSecurityContext(Credentials.SUB);
        applicationHistory.setChangedBy(changedBy);

        log.info("Application status changed: old={}, new={}, by={}", oldStatus, status, changedBy);
        applicationHistoryRepository.save(applicationHistory);
    }
}
