package org.yaroslaavl.recruitingservice.service;

import org.springframework.data.domain.Pageable;
import org.yaroslaavl.recruitingservice.database.entity.enums.*;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.VacancyShortDto;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface VacancyService {

    void create(VacancyRequestDto vacancyRequestDto);

    void delete(UUID vacancyId, UUID companyId);

    void update(UUID vacancyId, VacancyUpdateRequestDto vacancyUpdateRequestDto);

    VacancyResponseDto getVacancy(UUID vacancyId);

    PageShortDto<VacancyShortDto> getFilteredVacancies(String textSearch, ContractType contractType, WorkMode workMode, PositionLevel positionLevel, Workload workload, Integer salaryFrom, Integer salaryTo, LocalDate uploadAt, Pageable pageable);

    PageShortDto<VacancyShortDto> getCompanyVacancies(UUID companyId, Pageable pageable);

    Map<UUID, Long> countCompanyVacancies(Set<UUID> companyIds);
}
