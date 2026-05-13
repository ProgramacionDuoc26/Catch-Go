package com.catchandgo.jobs.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Service
public class NotificationPublisher {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public NotificationPublisher(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendNotification(String userId, String title, String message, String type) {
        try {
            Map<String, String> payload = Map.of(
                "title", title,
                "message", message,
                "type", type,
                "userId", userId
            );
            String json = objectMapper.writeValueAsString(payload);
            
            // Publish to a general channel or user-specific channel
            // The notification service will subscribe to these
            redisTemplate.convertAndSend("notifications", json);
            
            System.out.println("Published notification for user " + userId + ": " + title);
        } catch (Exception e) {
            System.err.println("Error publishing notification: " + e.getMessage());
        }
    }
}
