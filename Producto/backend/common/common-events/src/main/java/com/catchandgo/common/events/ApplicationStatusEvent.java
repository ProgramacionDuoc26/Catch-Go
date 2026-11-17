package com.catchandgo.common.events;

import java.time.Instant;
import java.util.UUID;

public record ApplicationStatusEvent(String eventId, Long applicationId, Long jobId,
        String workerId, String previousStatus, String newStatus, Instant occurredAt,
        String title, String message, String notificationType) {
    public static ApplicationStatusEvent create(Long applicationId, Long jobId, String workerId,
            String previousStatus, String newStatus, String title, String message, String notificationType) {
        return new ApplicationStatusEvent(UUID.randomUUID().toString(), applicationId, jobId, workerId,
                previousStatus, newStatus, Instant.now(), title, message, notificationType);
    }
}
