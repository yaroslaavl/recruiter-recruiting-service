package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyReportReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReportSystemResponseDto(
        @NotNull String userId,
        @NotBlank String vacancyTitle,
        @NotBlank String vacancyDescription,
        @NotNull String  vacancyId,
        @NotNull VacancyReportReason reportReason,
        @NotNull RecruitingSystemStatus status,
        String comment,
        @NotNull LocalDateTime reportedAt
) { }
