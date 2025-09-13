package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ApplicationShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.service.ApplicationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/apply")
    public ResponseEntity<Void> apply(@RequestBody VacancyApplicationRequestDto vacancyApplicationRequestDto) {
        applicationService.applyVacancy(vacancyApplicationRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/{vacancyId}")
    public ResponseEntity<PageShortDto<ApplicationShortDto>> getApplications(@PathVariable("vacancyId") UUID vacancyId,
                                                                             @RequestParam(required = false, name = "status") RecruitingSystemStatus status,
                                                                             @RequestParam(required = false) String salary,
                                                                             @RequestParam(required = false) String workMode,
                                                                             @RequestParam(required = false) Integer availableHoursPerWeek,
                                                                             @RequestParam(required = false) String availableFrom,
                                                                             @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(applicationService.getFilteredApplications(vacancyId, status, salary, workMode, availableHoursPerWeek, availableFrom, pageable));
    }

    @GetMapping("/{id}/info")
    public ResponseEntity<ApplicationDetailsResponseDto> getApplicationDetails(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(applicationService.getApplicationDetails(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> changeApplicationStatus(@PathVariable("id") UUID id, @RequestParam("newStatus") RecruitingSystemStatus newStatus) {
        applicationService.changeApplicationStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }
}
