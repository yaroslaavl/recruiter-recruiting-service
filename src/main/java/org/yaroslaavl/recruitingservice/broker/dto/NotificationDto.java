package org.yaroslaavl.recruitingservice.broker.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.Map;

@Builder
public record NotificationDto(
        String userId,
        @NotBlank String targetUserId,
        String entityId,
        @NotBlank String entityType,
        @NotBlank String notificationType,
        String content,
        Map<String, String> contentVariables
) { }
