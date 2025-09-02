package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemShortDto;
import org.yaroslaavl.recruitingservice.service.ReportSystemService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report-system")
public class ReportSystemController {

    private final ReportSystemService reportSystemService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendReport(@RequestBody ReportRequestDto reportRequestDto) {
        reportSystemService.report(reportRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> resolveReport(@PathVariable("id") UUID id, @RequestParam("status") RecruitingSystemStatus newStatus) {
        reportSystemService.resolveReport(id, newStatus);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/")
    public ResponseEntity<Page<ReportSystemShortDto>> getReports(@RequestParam("vacancyId") UUID vacancyId,
                                                                 @RequestParam(required = false, value = "status") RecruitingSystemStatus status,
                                                                 @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                                 @RequestParam(value = "page", defaultValue = "0") Integer page) {
        Pageable pageable = Pageable.ofSize(size).withPage(page);
        return ResponseEntity.ok(reportSystemService.getFilteredReports(vacancyId, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportSystemResponseDto> getReport(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(reportSystemService.getReport(id));
    }
}
