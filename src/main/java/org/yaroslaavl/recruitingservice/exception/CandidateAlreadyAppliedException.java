package org.yaroslaavl.recruitingservice.exception;

public class CandidateAlreadyAppliedException extends RuntimeException {
    public CandidateAlreadyAppliedException(String message) {
        super(message);
    }
}
