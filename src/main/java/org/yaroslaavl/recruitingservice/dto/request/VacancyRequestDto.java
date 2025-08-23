package org.yaroslaavl.recruitingservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.yaroslaavl.recruitingservice.database.entity.enums.ContractType;
import org.yaroslaavl.recruitingservice.database.entity.enums.PositionLevel;
import org.yaroslaavl.recruitingservice.database.entity.enums.WorkMode;
import org.yaroslaavl.recruitingservice.database.entity.enums.Workload;

import java.util.UUID;

public record VacancyRequestDto(
        @NotNull UUID companyId,
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 100) String title,
        @NotBlank @Size(max = 300) String description,
        @Size(max = 300) String requirementsMustHave,
        @Size(max = 300) String requirementsNiceToHave,
        @NotNull ContractType contractType,
        @NotNull WorkMode workMode,
        @NotNull PositionLevel positionLevel,
        @NotNull Workload workload,
        String location,
        Integer salaryFrom,
        Integer salaryTo
) { }
