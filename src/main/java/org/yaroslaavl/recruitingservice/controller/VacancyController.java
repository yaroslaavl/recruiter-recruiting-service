package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.service.VacancyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createVacancy(@RequestBody VacancyRequestDto vacancyRequestDto) {
        vacancyService.createVacancy(vacancyRequestDto);
        return ResponseEntity.noContent().build();
    }
}
