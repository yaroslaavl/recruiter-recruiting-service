package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "VacancyReportReason")
public enum VacancyReportReason {
    FRAUD,
    DUPLICATE,
    INAPPROPRIATE,
    MISLEADING_INFO,
    SPAM,
    DISCRIMINATION,
    OTHER
}

