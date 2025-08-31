package org.yaroslaavl.recruitingservice.dto.response;

import java.time.LocalDateTime;

public record ReportSystemLimitInfo(
        Long maxReportsPerTimeSpan,
        LocalDateTime availableAgainAt
) { }
