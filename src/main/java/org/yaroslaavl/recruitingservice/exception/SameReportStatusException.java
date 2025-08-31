package org.yaroslaavl.recruitingservice.exception;

public class SameReportStatusException extends RuntimeException {
    public SameReportStatusException(String message) {
        super(message);
    }
}
