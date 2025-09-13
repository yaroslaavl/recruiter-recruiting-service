package org.yaroslaavl.recruitingservice.feignClient.dto;

import jakarta.validation.constraints.NotBlank;

public record UserFeignDto(
        @NotBlank String userId,
        @NotBlank String displayName,
        String salary,
        String workMode,
        Integer availableHoursPerWeek,
        String availableFrom
) { }
