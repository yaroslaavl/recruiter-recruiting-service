package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ApplicationShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.CandidateApplicationsShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.feignClient.dto.ApplicationChatInfo;
import org.yaroslaavl.recruitingservice.service.ApplicationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('VERIFIED_CANDIDATE')")
    public ResponseEntity<Void> apply(@RequestBody VacancyApplicationRequestDto vacancyApplicationRequestDto) {
        applicationService.applyVacancy(vacancyApplicationRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/{vacancyId}")
    @PreAuthorize("hasRole('VERIFIED_RECRUITER')")
    public ResponseEntity<PageShortDto<ApplicationShortDto>> getApplications(
            @PathVariable("vacancyId") UUID vacancyId,
            @RequestParam(required = false, name = "status") RecruitingSystemStatus status,
            @RequestParam(required = false) String salary,
            @RequestParam(required = false) String workMode,
            @RequestParam(required = false) Integer availableHoursPerWeek,
            @RequestParam(required = false) String availableFrom,
            @PageableDefault(size = 15) Pageable pageable
    ) {
        return ResponseEntity.ok(
                applicationService.getFilteredApplications(vacancyId, status, salary, workMode, availableHoursPerWeek, availableFrom, pageable)
        );
    }

    @GetMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('VERIFIED_CANDIDATE', 'VERIFIED_RECRUITER') and @accessChecker.hasAccessToApplication(#id)")
    public ResponseEntity<ApplicationDetailsResponseDto> getApplicationDetails(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationDetails(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('VERIFIED_RECRUITER') and @accessChecker.hasAccessToChangeApplicationStatus(#id)")
    public ResponseEntity<Void> changeApplicationStatus(@PathVariable("id") UUID id,
                                                        @RequestParam("newStatus") RecruitingSystemStatus newStatus) {
        applicationService.changeApplicationStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('VERIFIED_CANDIDATE')")
    public ResponseEntity<PageShortDto<CandidateApplicationsShortDto>> getMyApplications(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(applicationService.getMyApplications(pageable));
    }

    @GetMapping("/chat-open")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public boolean isOpenedForChatting(@RequestParam("applicationId") UUID applicationId) {
        return applicationService.isOpenedForChatting(applicationId);
    }

    @GetMapping("/chat-previews")
    @PreAuthorize("hasRole('INTERNAL_SERVICE')")
    public ResponseEntity<List<ApplicationChatInfo>> getPreviewApplications(@RequestParam("applicationIds") Set<UUID> applicationIds) {
        return ResponseEntity.ok(applicationService.getPreviewApplicationInfo(applicationIds));
    }
}
