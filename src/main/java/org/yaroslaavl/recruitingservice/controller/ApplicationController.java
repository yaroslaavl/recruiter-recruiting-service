package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.VacancyApplicationRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationDetailsResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.ApplicationResponseDto;
import org.yaroslaavl.recruitingservice.service.ApplicationService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<Void> apply(@RequestBody VacancyApplicationRequestDto vacancyApplicationRequestDto) {
        applicationService.applyVacancy(vacancyApplicationRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{vacancyId}")
    public ResponseEntity<Page<ApplicationResponseDto>> getApplications(@PathVariable("vacancyId") UUID vacancyId,
                                                                        @RequestParam(required = false, value = "status") RecruitingSystemStatus status,
                                                                        @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                                        @RequestParam(value = "page", defaultValue = "0") Integer page) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ResponseEntity.ok(applicationService.findFilteredApplications(vacancyId, status, pageable));
    }

    @GetMapping("/info")
    public ResponseEntity<ApplicationDetailsResponseDto> getApplicationDetails(@RequestParam("applicationId") UUID applicationId) {
        return ResponseEntity.ok(applicationService.getApplicationDetails(applicationId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> changeApplicationStatus(@PathVariable("id") UUID id, @RequestParam("newStatus") RecruitingSystemStatus newStatus) {
        applicationService.changeApplicationStatus(id, newStatus);
        return ResponseEntity.noContent().build();
    }
}
