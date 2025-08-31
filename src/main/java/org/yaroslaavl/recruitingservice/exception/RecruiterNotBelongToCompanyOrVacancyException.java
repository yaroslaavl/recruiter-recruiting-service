package org.yaroslaavl.recruitingservice.exception;

public class RecruiterNotBelongToCompanyOrVacancyException extends RuntimeException {
    public RecruiterNotBelongToCompanyOrVacancyException(String message) {
        super(message);
    }
}
