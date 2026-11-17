package com.catchandgo.jobs.messaging.admin.dto;
import java.util.List;
public record ConsumerGroupDto(String name, Long consumerCount, Long pendingMessages, String lastDeliveredId,
                               Long lag, List<ConsumerDto> consumers) {
    public record ConsumerDto(String name, long pendingMessages, long idleTimeMs) {}
}
