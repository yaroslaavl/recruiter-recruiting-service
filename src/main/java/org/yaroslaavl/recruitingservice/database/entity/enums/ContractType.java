package org.yaroslaavl.recruitingservice.database.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ContractType")
public enum ContractType {
    EMPLOYMENT_CONTRACT,
    MANDATE_CONTRACT,
    SPECIFIC_TASK_CONTRACT,
    B2B,
    INTERNSHIP,
    TEMPORARY,
}