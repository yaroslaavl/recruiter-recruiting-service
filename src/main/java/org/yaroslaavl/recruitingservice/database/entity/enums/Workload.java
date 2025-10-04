package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Workload")
public enum Workload {
    FULL_TIME,
    PART_TIME,
    TEMPORARY,
}
