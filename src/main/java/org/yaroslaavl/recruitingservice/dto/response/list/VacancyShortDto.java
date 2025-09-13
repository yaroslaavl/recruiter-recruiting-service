package org.yaroslaavl.recruitingservice.dto.response.list;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.ContractType;
import org.yaroslaavl.recruitingservice.database.entity.enums.PositionLevel;
import org.yaroslaavl.recruitingservice.database.entity.enums.WorkMode;
import org.yaroslaavl.recruitingservice.database.entity.enums.Workload;

import java.time.LocalDateTime;
import java.util.UUID;

public record VacancyShortDto(
        @NotNull UUID id,
        @NotBlank String category,
        @NotNull UUID companyId,
        @NotBlank String companyName,
        @NotBlank String companyLocation,
        @NotBlank String title,
        @NotNull ContractType contractType,
        @NotNull WorkMode workMode,
        @NotNull PositionLevel positionLevel,
        @NotNull Workload workload,
        String location,
        Integer salaryFrom,
        Integer salaryTo,
        String companyLogoUrl,
        @NotNull LocalDateTime createdAt
) { }
