package org.yaroslaavl.recruitingservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.time.LocalDate;
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
    @PreAuthorize("hasRole('VERIFIED_RECRUITER')")
    public ResponseEntity<Void> createVacancy(@RequestBody VacancyRequestDto vacancyRequestDto) {
        vacancyService.create(vacancyRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/info")
    @PreAuthorize("permitAll()")
    public ResponseEntity<VacancyResponseDto> getVacancy(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(vacancyService.getVacancy(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('VERIFIED_RECRUITER') and @accessChecker.hasAccessToActOnVacancy(#id)")
    public ResponseEntity<Void> update(@PathVariable("id") UUID id,
                                       @RequestBody VacancyUpdateRequestDto vacancyUpdateRequestDto) {
        vacancyService.update(id, vacancyUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('VERIFIED_RECRUITER') and @accessChecker.hasAccessToActOnVacancy(#id)")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id,
                                       @RequestParam("companyId") UUID companyId) {
        vacancyService.delete(id, companyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageShortDto<VacancyShortDto>> getFilteredVacancies(
            @RequestParam(required = false, defaultValue = "") String textSearch,
            @RequestParam(required = false) ContractType contractType,
            @RequestParam(required = false) WorkMode workMode,
            @RequestParam(required = false) PositionLevel positionLevel,
            @RequestParam(required = false) Workload workload,
            @RequestParam(required = false) Integer salaryFrom,
            @RequestParam(required = false) Integer salaryTo,
            @RequestParam(required = false) LocalDate uploadAt,
            @PageableDefault(size = 15) Pageable pageable
    ) {
        return ResponseEntity.ok(
                vacancyService.getFilteredVacancies(textSearch, contractType, workMode, positionLevel,
                        workload, salaryFrom, salaryTo, uploadAt, pageable)
        );
    }

    @GetMapping("/{id}/company")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageShortDto<VacancyShortDto>> getCompanyVacancies(@PathVariable("id") UUID id,
                                                                             @PageableDefault(size = 15) Pageable pageable) {
        return ResponseEntity.ok(vacancyService.getCompanyVacancies(id, pageable));
    }

    @GetMapping("/count")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<UUID, Long>> getCompanyVacanciesCount(@RequestParam("ids") Set<UUID> ids) {
        return ResponseEntity.ok(vacancyService.countCompanyVacancies(ids));
    }
}

