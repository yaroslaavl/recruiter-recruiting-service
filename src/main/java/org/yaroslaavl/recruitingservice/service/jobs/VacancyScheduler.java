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
