package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.ReportSystemShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.UserReportsShortDto;
import org.yaroslaavl.recruitingservice.service.ReportSystemService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report-system")
public class ReportSystemController {

    private final ReportSystemService reportSystemService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('VERIFIED_RECRUITER', 'VERIFIED_CANDIDATE')")
    public ResponseEntity<Void> sendReport(@RequestBody ReportRequestDto reportRequestDto) {
        reportSystemService.report(reportRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> resolveReport(@PathVariable("id") UUID id,
                                              @RequestParam("status") RecruitingSystemStatus newStatus) {
        reportSystemService.resolveReport(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('VERIFIED_RECRUITER')")
    public ResponseEntity<PageShortDto<ReportSystemShortDto>> getFilteredReports(@RequestParam("vacancyId") UUID vacancyId,
                                                                                 @RequestParam(required = false, value = "status") RecruitingSystemStatus status,
                                                                                 @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(reportSystemService.getFilteredReports(vacancyId, status, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER') or " +
            "(hasAnyRole('VERIFIED_CANDIDATE', 'VERIFIED_RECRUITER') and @accessChecker.hasAccessToReport(#id))")
    public ResponseEntity<ReportSystemResponseDto> getReport(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(reportSystemService.getReport(id));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('VERIFIED_RECRUITER', 'VERIFIED_CANDIDATE')")
    public ResponseEntity<PageShortDto<UserReportsShortDto>> getMyReports(@PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(reportSystemService.getMyReports(pageable));
    }
}

