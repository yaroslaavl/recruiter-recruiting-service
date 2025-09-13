package org.yaroslaavl.recruitingservice.dto.response.list;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportSystemShortDto(
        @NotNull UUID id,
        @NotBlank String displayName,
        @NotNull RecruitingSystemStatus status,
        @NotNull LocalDateTime reportedAt
) { }
