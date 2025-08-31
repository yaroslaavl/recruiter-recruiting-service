package org.yaroslaavl.recruitingservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;
import org.yaroslaavl.recruitingservice.dto.request.ReportRequestDto;
import org.yaroslaavl.recruitingservice.dto.response.ReportSystemResponseDto;

import java.util.UUID;

public interface ReportSystemService {

    void report(ReportRequestDto reportRequestDto);

    void resolveReport(UUID reportId, RecruitingSystemStatus newStatus);

    Page<ReportSystemResponseDto> getReports(UUID vacancyId, Pageable pageable);
}
