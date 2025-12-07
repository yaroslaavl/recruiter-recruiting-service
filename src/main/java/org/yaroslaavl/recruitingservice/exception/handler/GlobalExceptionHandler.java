package org.yaroslaavl.recruitingservice.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.yaroslaavl.recruitingservice.dto.response.AlreadyAppliedInfo;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemLimitInfo;
import org.yaroslaavl.recruitingservice.dto.response.VacancyAlreadyReportedInfo;
import org.yaroslaavl.recruitingservice.exception.CandidateAlreadyAppliedException;
import org.yaroslaavl.recruitingservice.exception.ErrorType;
import org.yaroslaavl.recruitingservice.exception.MaxReportsPerTimeSpanException;
import org.yaroslaavl.recruitingservice.exception.VacancyAlreadyReportedException;
import org.yaroslaavl.recruitingservice.exception.response.ErrorResponse;

import java.time.LocalDateTime;

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

    @ExceptionHandler(CandidateAlreadyAppliedException.class)
    public ResponseEntity<ErrorResponse<AlreadyAppliedInfo>> handleFileStorage(CandidateAlreadyAppliedException fs, HttpServletRequest request) {
        ErrorResponse<AlreadyAppliedInfo> errorResponse =
                new ErrorResponse<>(fs.getMessage(), new AlreadyAppliedInfo(ErrorType.CANDIDATE_ALREADY_APPLIED, LocalDateTime.now()));

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
