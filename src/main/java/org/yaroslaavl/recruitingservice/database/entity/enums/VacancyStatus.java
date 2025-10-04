package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "VacancyStatus")
public enum VacancyStatus {
    ENABLED,
    DISABLED,
    TEMP_DISABLED,
    TIME_EXPIRED,
    ARCHIVED,
}
