package org.yaroslaavl.recruitingservice.exception;

public class VacancyIsNotActiveException extends RuntimeException {
    public VacancyIsNotActiveException(String message) {
        super(message);
    }
}
