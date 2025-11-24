package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CategoryResponseDto(
        @NotNull UUID id,
        @NotBlank String name,
        @NotBlank String description
) { }
