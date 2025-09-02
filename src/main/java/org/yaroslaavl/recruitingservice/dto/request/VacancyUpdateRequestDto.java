package org.yaroslaavl.recruitingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.ContractType;
import org.yaroslaavl.recruitingservice.database.entity.enums.PositionLevel;
import org.yaroslaavl.recruitingservice.database.entity.enums.WorkMode;
import org.yaroslaavl.recruitingservice.database.entity.enums.Workload;

import java.util.UUID;

public record VacancyUpdateRequestDto(
        @NotNull UUID companyId,
        String description,
        String requirementsMustHave,
        String requirementsNiceToHave,
        ContractType contractType,
        WorkMode workMode,
        PositionLevel positionLevel,
        Workload workload,
        String location,
        Integer salaryFrom,
        Integer salaryTo
) { }
