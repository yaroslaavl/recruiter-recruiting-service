package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "PositionLevel")
public enum PositionLevel {
    INTERN,
    ASSISTANT,
    JUNIOR,
    MID,
    MANAGER,
    DIRECTOR,
    WORKER,
    SENIOR,
}
