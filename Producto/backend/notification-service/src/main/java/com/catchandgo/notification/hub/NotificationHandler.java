package com.catchandgo.notification.hub;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Component
public class NotificationHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationHandler(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    public void handleMessage(String message) {
        try {
            Map<String, String> data = objectMapper.readValue(message, Map.class);
            String userId = data.get("userId");
            
            // Send to user-specific topic
            messagingTemplate.convertAndSend("/topic/user/" + userId, data);
            
            System.out.println("Relayed notification to WebSocket for user " + userId);
        } catch (Exception e) {
            System.err.println("Error handling Redis message: " + e.getMessage());
        }
    }
}
