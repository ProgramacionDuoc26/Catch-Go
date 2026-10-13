package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JobApplicationStreamConsumerTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MessagingProperties messagingProperties;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private JobApplicationStreamConsumer consumer;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(messagingProperties.getGroupName(anyString(), anyString())).thenReturn("notifications-group");
        when(messagingProperties.getStreamName(eq("dlq"), anyString())).thenReturn("stream:job-applications:dlq");

        MessagingProperties.Retry retry = new MessagingProperties.Retry();
        retry.setMaxAttempts(2);
        when(messagingProperties.getRetry()).thenReturn(retry);
    }

    @Test
    void onMessage_success_emitsAckAndWebSocket() {
        Map<String, String> payload = Map.of(
            "employerId", "company-123",
            "candidateId", "worker-456",
            "offerId", "1"
        );
        MapRecord<String, String, String> record = MapRecord.create("stream:job-applications", payload)
            .withId(RecordId.of("1710000000000-0"));

        consumer.onMessage(record);

        // Verifica que se envió la notificación por WebSocket a la empresa
        verify(messagingTemplate).convertAndSend(eq("/topic/user/company-123"), any(Map.class));

        // Verifica la confirmación explícita mediante XACK
        verify(streamOperations).acknowledge("stream:job-applications", "notifications-group", RecordId.of("1710000000000-0"));
    }

    @Test
    void onMessage_failure_routesToDlqAfterRetries() {
        // Mensaje con fallo forzado
        Map<String, String> payload = Map.of(
            "employerId", "company-123",
            "candidateId", "worker-456",
            "offerId", "1",
            "forceError", "true"
        );
        MapRecord<String, String, String> record = MapRecord.create("stream:job-applications", payload)
            .withId(RecordId.of("1710000000000-1"));

        // Intento 1: no debe ir a DLQ todavía
        consumer.onMessage(record);
        verify(streamOperations, never()).add(eq("stream:job-applications:dlq"), any(Map.class));

        // Intento 2 (alcanza maxAttempts=2): debe transferirse a la DLQ
        consumer.onMessage(record);
        verify(streamOperations).add(eq("stream:job-applications:dlq"), any(Map.class));

        // Y se debe confirmar en la cola principal para no bloquear otros mensajes
        verify(streamOperations).acknowledge("stream:job-applications", "notifications-group", RecordId.of("1710000000000-1"));
    }
}
