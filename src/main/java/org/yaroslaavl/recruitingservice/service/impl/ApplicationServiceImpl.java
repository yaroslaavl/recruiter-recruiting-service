package org.yaroslaavl.recruitingservice.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
import org.yaroslaavl.recruitingservice.dto.response.ApplicationResponseDto;
import org.yaroslaavl.recruitingservice.exception.*;
import org.yaroslaavl.recruitingservice.feignClient.cv.CvFeignClient;
import org.yaroslaavl.recruitingservice.feignClient.user.UserFeignClient;
import org.yaroslaavl.recruitingservice.mapper.ApplicationMapper;
import org.yaroslaavl.recruitingservice.service.ApplicationService;
import org.yaroslaavl.recruitingservice.service.SecurityContextService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    @Transactional
    public void applyVacancy(VacancyApplicationRequestDto vacancyApplicationRequestDto) {
        log.info("Applying vacancy: {}", vacancyApplicationRequestDto.vacancyId());

        String candidateKeyId = securityContextService.getSecurityContext(Credentials.SUB);
        boolean approved = userFeignClient.isApproved(candidateKeyId);

        if (!approved) {
            throw new CandidateAccountStatusException("User is not approved");
        }

        String cvForRecruiter = cvFeignClient.getCvForRecruiter(vacancyApplicationRequestDto.cvId());

        if (cvForRecruiter == null || cvForRecruiter.isEmpty()) {
            throw new GetCvException("Cv is not found or not readable");
        }

        Optional<Application> optionalApplication = applicationRepository.findByVacancyIdAndCandidateId(
                vacancyApplicationRequestDto.vacancyId(),
                candidateKeyId);

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
                applicationHistoryChanger(application, RecruitingSystemStatus.NEW, true);
            }
        } else {
            Vacancy vacancy = vacancyRepository.findById(vacancyApplicationRequestDto.vacancyId()).orElseThrow(
                    () -> new EntityNotFoundException("Vacancy with id: " + vacancyApplicationRequestDto.vacancyId() + " not found"));

            Application newApplication = Application.builder()
                    .vacancy(vacancy)
                    .cvId(vacancyApplicationRequestDto.cvId())
                    .candidateId(candidateKeyId)
                    .status(RecruitingSystemStatus.NEW)
                    .coverLetter(vacancyApplicationRequestDto.coverLetter())
                    .appliedAt(LocalDateTime.now())
                    .build();

            applicationRepository.save(newApplication);
            applicationHistoryChanger(newApplication, RecruitingSystemStatus.NEW, true);
        }

        //sendNotification
    }

    @Override
    public Page<ApplicationResponseDto> findFilteredApplications(UUID vacancyId, RecruitingSystemStatus status, Pageable pageable) {
        String recruiterKeyId = securityContextService.getSecurityContext(Credentials.SUB);

        Vacancy vacancy = vacancyRepository.findById(vacancyId).orElseThrow(
                () -> new EntityNotFoundException("Vacancy with id: " + vacancyId + " not found"));

        if (!recruiterKeyId.equals(vacancy.getRecruiterId())) {
            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to vacancy");
        }

        Page<Application> applicationsByVacancyIdAndStatus = applicationRepository.findApplicationsByVacancyIdAndStatus(vacancyId, status, pageable);

        return applicationsByVacancyIdAndStatus.map(applicationMapper::toApplicationDto);
    }

    @Override
    @Transactional
    public ApplicationDetailsResponseDto getApplicationDetails(UUID applicationId) {
        Application application = checkAndGetApplicationBack(applicationId);

        if (application.getStatus() == RecruitingSystemStatus.NEW) {
            application.setStatus(RecruitingSystemStatus.VIEWED);

            applicationHistoryChanger(application, RecruitingSystemStatus.VIEWED, false);
            applicationRepository.save(application);
            //send notification to candidate
        }
        return applicationMapper.toApplicationDetailsDto(application);
    }

    @Override
    @Transactional
    public void changeApplicationStatus(UUID applicationId, RecruitingSystemStatus newStatus) {
        Application application = checkAndGetApplicationBack(applicationId);

        if (application.getStatus() == RecruitingSystemStatus.NEW) {
            throw new ViewApplicationException("Recruiter can't change status of application");
        }

        EnumSet<RecruitingSystemStatus> allowed = ALLOWED_STATUSES.getOrDefault(application.getStatus(), EnumSet.noneOf(RecruitingSystemStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException("Cannot change application from " + application.getStatus() + " to " + newStatus);
        }

        application.setStatus(newStatus);
        applicationHistoryChanger(application, newStatus, false);

        if (newStatus == RecruitingSystemStatus.ACCEPTED) {
            application.getVacancy().setStatus(VacancyStatus.ARCHIVED);
            vacancyRepository.save(application.getVacancy());
            //send notification to recruiter and candidate
        }

        //send notification to candidate
        applicationRepository.save(application);
    }

    private Application checkAndGetApplicationBack(UUID applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException("Application not found"));

        if (!application.getVacancy().getRecruiterId()
                .equals(securityContextService.getSecurityContext(Credentials.SUB))) {
            throw new RecruiterNotBelongToCompanyOrVacancyException("Recruiter is not belong to vacancy");
        }
        return application;
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
