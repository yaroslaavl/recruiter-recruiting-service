package org.yaroslaavl.recruitingservice.exception;

import lombok.Getter;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemLimitInfo;

@Getter
public class MaxReportsPerTimeSpanException extends RuntimeException {

    private final ReportSystemLimitInfo reportSystemLimitInfo;

    public MaxReportsPerTimeSpanException(String message, ReportSystemLimitInfo reportSystemLimitInfo) {
        super(message);
        this.reportSystemLimitInfo = reportSystemLimitInfo;
    }
}
