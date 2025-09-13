package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationHistoryResponseDto(
        @NotNull UUID id,
        @NotNull RecruitingSystemStatus oldStatus,
        @NotNull RecruitingSystemStatus newStatus,
        String changedBy,
        @NotNull LocalDateTime changedAt
) { }
