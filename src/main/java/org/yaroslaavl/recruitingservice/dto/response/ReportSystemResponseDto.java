package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyReportReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportSystemResponseDto(
        @NotNull UUID id,
        @NotNull String userId,
        @NotNull VacancyReportReason reportReason,
        @NotNull RecruitingSystemStatus status,
        String comment,
        @NotNull LocalDateTime reportedAt
) { }
