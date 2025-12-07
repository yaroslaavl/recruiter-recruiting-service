package org.yaroslaavl.recruitingservice.dto.response;

import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyReportReason;
import org.yaroslaavl.recruitingservice.exception.ErrorType;

import java.time.LocalDateTime;

public record AlreadyAppliedInfo(
        ErrorType errorType,
        LocalDateTime reportedAt
) { }
