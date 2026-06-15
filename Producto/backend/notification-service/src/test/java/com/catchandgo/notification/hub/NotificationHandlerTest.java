package com.catchandgo.notification.hub;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
public class NotificationHandlerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private NotificationHandler notificationHandler;

    private String validJsonMessage;
    private Map<String, String> payload;

    @BeforeEach
    void setUp() {
        validJsonMessage = "{\"title\":\"Prueba\",\"message\":\"Mensaje de prueba\",\"type\":\"info\",\"userId\":\"user-123\"}";
        payload = new HashMap<>();
        payload.put("title", "Prueba");
        payload.put("message", "Mensaje de prueba");
        payload.put("type", "info");
        payload.put("userId", "user-123");
    }

    // Procesar un mensaje de notificación válido exitosamente
    // Verifica que se deserialice el JSON y se envíe a través de WebSocket al topic del usuario
    @Test
    void handleMessage_success() throws Exception {
        when(objectMapper.readValue(eq(validJsonMessage), any(TypeReference.class))).thenReturn(payload);

        notificationHandler.handleMessage(validJsonMessage);

        verify(objectMapper).readValue(eq(validJsonMessage), any(TypeReference.class));
        verify(messagingTemplate).convertAndSend(eq("/topic/user/user-123"), eq(payload));
    }

    // Manejar un error de deserialización de JSON
    // Verifica que el método capture la excepción de forma segura y no realice ningún envío
    @Test
    void handleMessage_fail_deserializationError() throws Exception {
        String invalidJson = "{invalid-json}";
        when(objectMapper.readValue(eq(invalidJson), any(TypeReference.class))).thenThrow(new RuntimeException("JSON error"));

        notificationHandler.handleMessage(invalidJson);

        verify(objectMapper).readValue(eq(invalidJson), any(TypeReference.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }
}
