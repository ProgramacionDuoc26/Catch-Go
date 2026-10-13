package com.catchandgo.common.events;

import java.time.Instant;

public record QueueSummaryDto(
    String queueName,
    Long messageCount,
    Integer consumerGroupsCount,
    String status,
    String firstEntryId,
    String lastEntryId,
    Instant checkedAt
) {}
