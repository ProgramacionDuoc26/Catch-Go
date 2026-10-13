package com.catchandgo.common.events;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
    String eventId,
    String recipientUserId,
    String title,
    String message,
    String type,
    String sourceService,
    Instant timestamp
) {
    public static NotificationEvent create(
            String recipientUserId,
            String title,
            String message,
            String type,
            String sourceService) {
        return new NotificationEvent(
            UUID.randomUUID().toString(),
            recipientUserId,
            title,
            message,
            type,
            sourceService,
            Instant.now()
        );
    }
}
