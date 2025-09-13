package org.yaroslaavl.recruitingservice.feignClient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CompanyPreviewFeignDto(
        @NotNull UUID id,
        @NotBlank String name,
        @NotBlank String location,
        String logoUrl
) { }
