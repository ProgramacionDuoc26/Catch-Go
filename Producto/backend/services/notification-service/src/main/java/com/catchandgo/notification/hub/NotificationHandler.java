package com.catchandgo.notification.hub;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class NotificationHandler {
    private static final Logger log = LoggerFactory.getLogger(NotificationHandler.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationHandler(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(String message) {
        try {
            Map<String, String> data = objectMapper.readValue(message, new TypeReference<Map<String, String>>() {});
            String userId = data.get("userId");
            if (userId == null || userId.isBlank()) throw new IllegalArgumentException("Falta el usuario destinatario");
            messagingTemplate.convertAndSend("/topic/user/" + userId, data);
        } catch (Exception e) {
            log.warn("No se pudo reenviar la notificacion local: {}", e.getClass().getSimpleName());
        }
    }
}
