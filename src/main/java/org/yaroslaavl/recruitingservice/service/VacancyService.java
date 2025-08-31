package org.yaroslaavl.recruitingservice.service;

import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;

import java.util.UUID;

public interface VacancyService {

    void create(VacancyRequestDto vacancyRequestDto);

    void delete(UUID vacancyId, UUID companyId);
}
