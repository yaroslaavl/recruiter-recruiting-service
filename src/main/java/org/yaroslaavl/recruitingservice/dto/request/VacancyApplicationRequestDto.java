package org.yaroslaavl.recruitingservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VacancyApplicationRequestDto(
        @NotNull UUID vacancyId,
        @NotNull UUID cvId,
        String coverLetter
) { }
