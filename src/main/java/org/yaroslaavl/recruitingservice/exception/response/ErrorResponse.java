package org.yaroslaavl.recruitingservice.exception.response;

public record ErrorResponse<T>(
        String message,
        T info
) { }
