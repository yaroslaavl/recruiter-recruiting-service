package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RecruitingSystemStatus")
public enum RecruitingSystemStatus {

    //APPLICATION
    NO_MORE_INTERESTS,

    //APPLICATION
    VIEWED,

    //APPLICATION
    IN_PROGRESS,

    //APPLICATION
    ACCEPTED,

    //REPORT SYSTEM
    RESOLVED,

    //APPLICATION AND REPORT SYSTEM
    NEW,

    //APPLICATION AND REPORT SYSTEM
    REJECTED
}
