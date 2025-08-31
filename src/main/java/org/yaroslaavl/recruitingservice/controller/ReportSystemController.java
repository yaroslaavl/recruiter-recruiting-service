package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.service.ReportSystemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/report-system")
public class ReportSystemController {

    private final ReportSystemService reportSystemService;

    @PostMapping("/send")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> sendReport(@RequestBody ReportRequestDto reportRequestDto) {
        reportSystemService.report(reportRequestDto);
        return ResponseEntity.noContent().build();
    }
}
