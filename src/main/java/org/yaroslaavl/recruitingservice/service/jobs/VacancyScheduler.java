package org.yaroslaavl.recruitingservice.service.jobs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yaroslaavl.recruitingservice.database.entity.Vacancy;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyStatus;
import org.yaroslaavl.recruitingservice.database.repository.VacancyRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class VacancyScheduler {

    @Value("${vacancy.max_report_count}")
    private Long maxReportCount;

    @Value("${vacancy.time_expiration}")
    private Duration vacancyTimeExpiration;

    private final VacancyRepository vacancyRepository;

    /**
     * Checks and updates the activation status of vacancies that are not currently active.
     * This method runs at a fixed interval of 60 seconds and performs the following steps:
     *
     * 1. Retrieves a list of all non-active vacancies from the vacancy repository.
     * 2. Logs the retrieved vacancies and their current statuses.
     * 3. Skips vacancies with unresolved report counts exceeding the allowed maximum.
     * 4. For vacancies that have been in their current status for more than 15 minutes:
     *    a. Updates their status to "ENABLED".
     *    b. Marks them as no longer waiting for approval.
     *    c. Adds them to a list of changed vacancies.
     *    d. Logs the change of their status.
     * 5. Saves any updated vacancies back to the repository.
     *
     * The method ensures that only eligible vacancies are activated and notifies
     * recruiters for vacancies whose status has been updated.
     */
    @Scheduled(fixedDelay = 60000)
    public void checkVacancyActivationStatus() {
        log.info("Checking vacancy status");

        List<Vacancy> allNotActiveVacancies = vacancyRepository.findAllNotActiveVacancies();

        if (allNotActiveVacancies.isEmpty()) {
            log.info("Found 0 vacancies that are not active");
            return;
        }

        List<Vacancy> changedVacancies = new ArrayList<>();
        for (Vacancy vacancy : allNotActiveVacancies) {
            log.info("Vacancy with id '{}' is not active", vacancy.getId());

            if (vacancy.getNotResolvedReports() >= maxReportCount) {
                log.info("Vacancy with id '{}' is not active and has more than {} reports", vacancy.getId(), maxReportCount);
                continue;
            }

            if (vacancy.getLastStatusChangeAt().plusMinutes(15).isBefore(LocalDateTime.now())) {
                vacancy.setStatus(VacancyStatus.ENABLED);
                vacancy.setWaitingForApproval(Boolean.FALSE);

                changedVacancies.add(vacancy);
                log.info("Vacancy with id '{}' is now enabled", vacancy.getId());
                //send notification to Recruiter
            }
        }

        if (!changedVacancies.isEmpty()) {
            vacancyRepository.saveAll(changedVacancies);
        }
    }

    /**
     * This method periodically checks the status and expiration time of active vacancies.
     * It runs at a fixed interval of 60 seconds and performs the following operations:
     *
     * 1. Retrieves all active vacancies from the repository. If no active vacancies are found, it logs this information and exits.
     * 2. Iterates through each active vacancy and performs the following checks:
     *    a. If the number of unresolved reports for a vacancy exceeds the maximum allowed report count,
     *       the vacancy status is updated to `TEMP_DISABLED`, it is marked as waiting for approval,
     *       and added to a list of changed vacancies.
     *    b. If the current time exceeds the allocated expiration time for a vacancy since its last status change,
     *       the vacancy status is updated to `TIME_EXPIRED`, it is marked as waiting for approval,
     *       and added to the list of changed vacancies.
     * 3. Logs the ID of each processed vacancy along with its updated status if changes occurred.
     * 4. If vacancies were changed, the changes are persisted to the repository.
     * 5. Notifications to recruiters may be triggered for vacancies whose status has been updated.
     */
    @Scheduled(fixedDelay = 60000)
    public void checkVacancyReportsStatusAndExpirationTime() {
        log.info("Checking vacancy reports status");

        List<Vacancy> allActiveVacancies = vacancyRepository.findAllActiveVacancies();

        if (allActiveVacancies.isEmpty()) {
            log.info("Found 0 vacancies that are active");
            return;
        }

        List<Vacancy> changedVacancies = new ArrayList<>();
        boolean isExpired = Boolean.FALSE;
        boolean isChanged = Boolean.FALSE;

        for (Vacancy vacancy : allActiveVacancies) {
            log.info("Vacancy with id '{}' is active", vacancy.getId());

            if (vacancy.getNotResolvedReports() >= maxReportCount) {
                vacancy.setStatus(VacancyStatus.TEMP_DISABLED);
                vacancy.setWaitingForApproval(Boolean.TRUE);

                changedVacancies.add(vacancy);

                isChanged = Boolean.TRUE;
                log.info("Vacancy with id '{}' is now disabled", vacancy.getId());
            }

            if (vacancy.getLastStatusChangeAt().plusMinutes(vacancyTimeExpiration.toMinutes()).isBefore(LocalDateTime.now())) {
                vacancy.setStatus(VacancyStatus.TIME_EXPIRED);
                vacancy.setWaitingForApproval(Boolean.TRUE);

                changedVacancies.add(vacancy);

                isChanged = Boolean.TRUE;
                isExpired = Boolean.TRUE;
                log.info("Vacancy with id '{}' expired", vacancy.getId());
            }

            if (isChanged && isExpired) {
                //send notification to Recruiter
            } else if (isChanged) {
                //send notification to Recruiter
            }
        }

        if (!changedVacancies.isEmpty()) {
            vacancyRepository.saveAll(changedVacancies);
        }
    }
}
