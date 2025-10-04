package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "WorkMode")
public enum WorkMode {
    ONSITE,
    HYBRID,
    REMOTE,
}
