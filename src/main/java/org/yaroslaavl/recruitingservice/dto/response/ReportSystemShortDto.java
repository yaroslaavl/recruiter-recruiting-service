package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportSystemShortDto(
        @NotNull UUID id,
        @NotNull String userId,
        @NotNull RecruitingSystemStatus status,
        @NotNull LocalDateTime reportedAt
) { }
