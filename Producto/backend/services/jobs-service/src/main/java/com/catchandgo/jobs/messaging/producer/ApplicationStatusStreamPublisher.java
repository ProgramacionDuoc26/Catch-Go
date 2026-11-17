package com.catchandgo.jobs.messaging.producer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class ApplicationStatusStreamPublisher {
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;
    private final MessagingProperties properties;

    public ApplicationStatusStreamPublisher(StringRedisTemplate redis, ObjectMapper mapper, MessagingProperties properties) {
        this.redis = redis;
        this.mapper = mapper;
        this.properties = properties;
    }

    public void publish(ApplicationStatusEvent event) {
        String stream = properties.getStreamName("application-status", "stream:application-status");
        final String payload;
        try {
            payload = mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo serializar el evento de estado", e);
        }
        redis.opsForStream().add(MapRecord.create(stream, Map.of(
                "eventId", event.eventId(), "type", "APPLICATION_STATUS_CHANGED", "payload", payload)));
        Long maxLen = properties.getRetention().getMaxLen();
        if (maxLen != null && maxLen > 0) {
            redis.opsForStream().trim(stream, maxLen, true);
        }
    }
}
