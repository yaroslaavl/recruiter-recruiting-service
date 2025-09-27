package org.yaroslaavl.recruitingservice.service;

import org.springframework.data.domain.Pageable;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ApplicationShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.CandidateApplicationsShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.ApplicationChatInfo;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ApplicationService {

    void applyVacancy(VacancyApplicationRequestDto vacancyApplicationRequestDto);

    PageShortDto<ApplicationShortDto> getFilteredApplications(UUID vacancyId, RecruitingSystemStatus status, String salary, String workMode, Integer availableHoursPerWeek, String availableForm, Pageable pageable);

    ApplicationDetailsResponseDto getApplicationDetails(UUID applicationId);

    void changeApplicationStatus(UUID applicationId, RecruitingSystemStatus newStatus);

    PageShortDto<CandidateApplicationsShortDto> getMyApplications(Pageable pageable);

    boolean isOpenedForChatting(UUID applicationId);

    List<ApplicationChatInfo> getPreviewApplicationInfo(Set<UUID> applicationIds);
}
