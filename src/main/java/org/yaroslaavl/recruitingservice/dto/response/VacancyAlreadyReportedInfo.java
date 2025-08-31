package org.yaroslaavl.recruitingservice.dto.response;

import org.yaroslaavl.recruitingservice.database.entity.enums.VacancyReportReason;

import java.time.LocalDateTime;

public record VacancyAlreadyReportedInfo(
        VacancyReportReason vacancyReportReason,
        LocalDateTime reportedAt
) { }
