package org.yaroslaavl.recruitingservice.exception;

import lombok.Getter;
import org.yaroslaavl.recruitingservice.dto.response.VacancyAlreadyReportedInfo;

@Getter
public class VacancyAlreadyReportedException extends RuntimeException {

    private final VacancyAlreadyReportedInfo vacancyAlreadyReportedInfo;

    public VacancyAlreadyReportedException(String message, VacancyAlreadyReportedInfo vacancyAlreadyReportedInfo) {
        super(message);
        this.vacancyAlreadyReportedInfo = vacancyAlreadyReportedInfo;
    }
}
