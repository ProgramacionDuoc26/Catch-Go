package com.catchandgo.notification.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Difunde la notificacion a los brokers WebSocket locales de las instancias activas. */
@Component
public class NotificationBroadcaster {
    public static final String CHANNEL = "notifications";
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper;

    public NotificationBroadcaster(StringRedisTemplate redis, ObjectMapper mapper) {
        this.redis = redis;
        this.mapper = mapper;
    }

    public void publish(Map<String, String> notification) {
        final String payload;
        try {
            payload = mapper.writeValueAsString(notification);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar la notificacion", e);
        }
        Long listeners = redis.convertAndSend(CHANNEL, payload);
        if (listeners == null || listeners == 0) {
            throw new IllegalStateException("No hay instancias escuchando notificaciones");
        }
    }
}
