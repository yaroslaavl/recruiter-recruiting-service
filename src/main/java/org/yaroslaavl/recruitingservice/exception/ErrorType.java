package org.yaroslaavl.recruitingservice.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ErrorType")
public enum ErrorType {
    CANDIDATE_ALREADY_APPLIED,
}
