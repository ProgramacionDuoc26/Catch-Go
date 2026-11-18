package com.catchandgo.jobs.messaging.producer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.catchandgo.jobs.messaging.config.StreamRetentionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ApplicationStatusStreamPublisher {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationStatusStreamPublisher.class);
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final MessagingProperties properties;
    private final StreamRetentionService retention;

    public ApplicationStatusStreamPublisher(StringRedisTemplate redis, ObjectMapper mapper, MessagingProperties properties, StreamRetentionService retention) {
        this.redis = redis;
        this.mapper = mapper;
        this.properties = properties;
        this.retention = retention;
    }

    public RecordId publish(ApplicationStatusEvent event) {
        String stream = properties.getStreamName("application-status", "stream:application-status");
        final String payload;
        try {
            payload = mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el evento de estado", e);
        }
        Long maxLen = retention.effectiveMaxLen(stream);
        RecordId id = redis.opsForStream().add(MapRecord.create(stream, Map.of(
                "eventId", event.eventId(), "type", "APPLICATION_STATUS_CHANGED", "payload", payload)));
        if (id == null) throw new IllegalStateException("Redis no confirmo el evento");
        if (maxLen != null && maxLen > 0) {
            try {
                retention.trimSafely(stream, maxLen);
            } catch (Exception e) {
                log.warn("Evento publicado, pero no se pudo aplicar retencion en {}: {}", stream, e.getMessage());
            }
        }
        return id;
    }
}
