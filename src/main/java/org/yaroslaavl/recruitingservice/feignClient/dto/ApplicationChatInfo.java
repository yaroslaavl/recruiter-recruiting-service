package org.yaroslaavl.recruitingservice.feignClient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApplicationChatInfo(
        @NotNull UUID applicationId,
        @NotBlank String vacancyTitle,
        String companyLogoUrl
) { }
