package org.yaroslaavl.recruitingservice.service;

public interface VacancyService {

    void createVacancy();
    void updateVacancy();
    void updateVacancyStatus();
    void findFilteredVacancies();
    void findVacancy();
    void reportVacancy();
    void deleteVacancy();
}
