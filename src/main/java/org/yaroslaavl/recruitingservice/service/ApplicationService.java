package org.yaroslaavl.recruitingservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationResponseDto;

import java.util.UUID;

public interface ApplicationService {

    void applyVacancy(VacancyApplicationRequestDto vacancyApplicationRequestDto);

    Page<ApplicationResponseDto> findFilteredApplications(UUID vacancyId, RecruitingSystemStatus status, Pageable pageable);

    ApplicationDetailsResponseDto getApplicationDetails(UUID applicationId);

    void changeApplicationStatus(UUID applicationId, RecruitingSystemStatus newStatus);
}
