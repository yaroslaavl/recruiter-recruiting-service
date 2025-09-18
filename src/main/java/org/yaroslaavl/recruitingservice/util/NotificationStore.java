package org.yaroslaavl.recruitingservice.util;

import lombok.experimental.UtilityClass;
import org.yaroslaavl.recruitingservice.broker.dto.NotificationDto;

import java.util.Map;

@UtilityClass
public class NotificationStore {

    public static NotificationDto inAppNotification(String userId, String targetUserId, String entityId, String entityType, Map<String, String> properties) {
        return NotificationDto.builder()
                .userId(userId)
                .targetUserId(targetUserId)
                .entityId(entityId)
                .entityType(entityType)
                .notificationType("DASHBOARD_APP")
                .contentVariables(properties)
                .build();
    }
}
