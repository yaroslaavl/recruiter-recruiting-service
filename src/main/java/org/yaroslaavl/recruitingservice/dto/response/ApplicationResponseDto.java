package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponseDto(
        @NotNull UUID id,
        @NotBlank String candidateId,
        @NotNull RecruitingSystemStatus status,
        @NotNull LocalDateTime appliedAt
) { }
