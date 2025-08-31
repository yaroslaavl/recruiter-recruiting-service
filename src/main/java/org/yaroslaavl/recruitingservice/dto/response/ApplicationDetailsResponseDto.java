package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.yaroslaavl.recruitingservice.database.entity.enums.RecruitingSystemStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ApplicationDetailsResponseDto(
        @NotNull UUID id,
        @NotNull UUID vacancyId,
        @NotBlank String candidateId,
        @NotNull RecruitingSystemStatus status,
        String coverLetter,
        @NotNull LocalDateTime appliedAt,
        @NotNull List<ApplicationHistoryResponseDto> history
) { }
