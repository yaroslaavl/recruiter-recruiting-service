package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.service.VacancyService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    @PostMapping
    public ResponseEntity<Void> createVacancy(@RequestBody VacancyRequestDto vacancyRequestDto) {
        vacancyService.create(vacancyRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacancyResponseDto> getVacancy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vacancyService.getVacancy(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") UUID id, @RequestBody VacancyUpdateRequestDto vacancyUpdateRequestDto) {
        vacancyService.update(id, vacancyUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id, @RequestParam("companyId") UUID companyId) {
        vacancyService.delete(id, companyId);
        return ResponseEntity.noContent().build();
    }
}
