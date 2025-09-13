package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaroslaavl.recruitingservice.database.entity.enums.ContractType;
import org.yaroslaavl.recruitingservice.database.entity.enums.PositionLevel;
import org.yaroslaavl.recruitingservice.database.entity.enums.WorkMode;
import org.yaroslaavl.recruitingservice.database.entity.enums.Workload;
import org.yaroslaavl.recruitingservice.dto.request.VacancyRequestDto;
import org.yaroslaavl.recruitingservice.dto.request.VacancyUpdateRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.VacancyResponseDto;
import org.yaroslaavl.recruitingservice.dto.response.list.PageShortDto;
import org.yaroslaavl.recruitingservice.dto.response.list.VacancyShortDto;
import org.yaroslaavl.recruitingservice.service.VacancyService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vacancies")
public class VacancyController {

    private final VacancyService vacancyService;

    @PostMapping("/create")
    public ResponseEntity<Void> createVacancy(@RequestBody VacancyRequestDto vacancyRequestDto) {
        vacancyService.create(vacancyRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/info")
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

    @GetMapping("/search")
    public ResponseEntity<PageShortDto<VacancyShortDto>> getFilteredVacancies(
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) PositionLevel positionLevel,
            @RequestParam(required = false) Workload workload,
            @RequestParam(required = false) Integer salaryFrom,
            @RequestParam(required = false) Integer salaryTo,
            @RequestParam(required = false) LocalDateTime uploadAt,
            @PageableDefault(size = 15) Pageable pageable
    ) {
        PageShortDto<VacancyShortDto> vacancies = vacancyService.getFilteredVacancies(
                textSearch,
                contractType,
                workMode,
                positionLevel,
                workload,
                salaryFrom,
                salaryTo,
                uploadAt,
                pageable
        );
        return ResponseEntity.ok(vacancies);
    }

    @GetMapping("/{id}/company")
    public ResponseEntity<PageShortDto<VacancyShortDto>> getCompanyVacancies(@PathVariable("id") UUID id,
                                                                             @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(vacancyService.getCompanyVacancies(id, pageable));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<UUID, Long>> getCompanyVacanciesCount(@RequestParam("ids") Set<UUID> ids) {
        return ResponseEntity.ok(vacancyService.countCompanyVacancies(ids));
    }
}
