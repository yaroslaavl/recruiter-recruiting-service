package org.yaroslaavl.recruitingservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyReportReason;

import java.util.UUID;

public record ReportRequestDto(
        @NotNull UUID vacancyId,
        @NotNull VacancyReportReason vacancyReportReason,
        String comment
) { }
