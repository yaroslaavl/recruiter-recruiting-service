package org.yaroslaavl.recruitingservice.dto.response.list;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CandidateApplicationsShortDto(
        @NotNull UUID id,
        @NotNull UUID vacancyId,
        @NotBlank String companyName,
        @NotBlank String companyLocation,
        String companyLogoUrl,
        String location,
        Integer applicationNumber,
        @NotNull RecruitingSystemStatus status,
        @NotNull LocalDateTime finishDate
) { }
