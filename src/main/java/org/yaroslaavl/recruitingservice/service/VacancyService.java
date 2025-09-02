package org.yaroslaavl.recruitingservice.service;

import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;

import java.util.UUID;

public interface VacancyService {

    void create(VacancyRequestDto vacancyRequestDto);

    void delete(UUID vacancyId, UUID companyId);

    void update(UUID vacancyId, VacancyUpdateRequestDto vacancyUpdateRequestDto);

    VacancyResponseDto getVacancy(UUID vacancyId);
}
