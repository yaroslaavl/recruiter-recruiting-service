package org.yaroslaavl.recruitingservice.exception;

public class RecruiterNotBelongToCompanyException extends RuntimeException {
    public RecruiterNotBelongToCompanyException(String message) {
        super(message);
    }
}
