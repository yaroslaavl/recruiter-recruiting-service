package org.yaroslaavl.recruitingservice.exception.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemLimitInfo;
import org.yaroslaavl.recruitingservice.dto.response.VacancyAlreadyReportedInfo;
import org.yaroslaavl.recruitingservice.exception.MaxReportsPerTimeSpanException;
import org.yaroslaavl.recruitingservice.exception.VacancyAlreadyReportedException;
import org.yaroslaavl.recruitingservice.exception.response.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = MaxReportsPerTimeSpanException.class)
    public ResponseEntity<ErrorResponse<ReportSystemLimitInfo>> handleMaxReportsPerTimeSpanException(MaxReportsPerTimeSpanException mr) {
        ErrorResponse<ReportSystemLimitInfo> errorResponse =
                new ErrorResponse<>(mr.getMessage(), mr.getReportSystemLimitInfo());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(value = VacancyAlreadyReportedException.class)
    public ResponseEntity<ErrorResponse<VacancyAlreadyReportedInfo>> handleVacancyAlreadyReportedException(VacancyAlreadyReportedException vr) {
        ErrorResponse<VacancyAlreadyReportedInfo> errorResponse =
                new ErrorResponse<>(vr.getMessage(), vr.getVacancyAlreadyReportedInfo());

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
