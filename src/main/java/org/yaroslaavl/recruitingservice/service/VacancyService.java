package org.yaroslaavl.recruitingservice.service;

import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;

public interface VacancyService {

    void createVacancy(VacancyRequestDto vacancyRequestDto);
}
