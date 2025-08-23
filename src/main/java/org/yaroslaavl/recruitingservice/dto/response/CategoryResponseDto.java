package org.yaroslaavl.recruitingservice.dto.response;

import jakarta.validation.constraints.NotBlank;

public record CategoryResponseDto(
        @NotBlank String name,
        @NotBlank String description
) { }
