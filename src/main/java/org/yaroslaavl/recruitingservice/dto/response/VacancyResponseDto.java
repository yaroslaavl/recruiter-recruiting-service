package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.yaroslaavl.recruitingservice.database.entity.enums.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record VacancyResponseDto(
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
        Integer salaryTo,
        @NotNull VacancyStatus status,
        @NotNull boolean isWaitingForApproval,
        @NotNull LocalDateTime createdAt
) { }
