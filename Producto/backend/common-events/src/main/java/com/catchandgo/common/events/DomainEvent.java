package com.catchandgo.common.events;

import java.time.Instant;

public record DomainEvent(String type, String aggregateId, Instant occurredAt, String payload) {
}
