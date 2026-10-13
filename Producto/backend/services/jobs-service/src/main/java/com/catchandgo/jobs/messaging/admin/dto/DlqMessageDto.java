package com.catchandgo.jobs.messaging.admin.dto;

import java.util.Map;

/**
 * DTO que representa un mensaje capturado en la Dead Letter Queue (DLQ).
 */
public record DlqMessageDto(
    String messageId,
    String streamName,
    Map<String, String> payload,
    String errorReason,
    String failedAt
) {}
