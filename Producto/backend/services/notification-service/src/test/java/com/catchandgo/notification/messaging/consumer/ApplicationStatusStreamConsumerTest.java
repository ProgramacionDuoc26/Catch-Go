package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ApplicationStatusStreamConsumerTest {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, String, String> operations = mock(StreamOperations.class);
    SimpMessagingTemplate websocket = mock(SimpMessagingTemplate.class);
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    MessagingProperties properties = new MessagingProperties();
    ApplicationStatusStreamConsumer consumer;

    @BeforeEach void setup() {
        when(redis.<String, String>opsForStream()).thenReturn(operations);
        consumer = new ApplicationStatusStreamConsumer(redis, websocket, mapper, properties);
    }

    @Test void deliversAndAcknowledges() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        MapRecord<String, String, String> record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("1-0"));
        consumer.onMessage(record);
        verify(websocket).convertAndSend(eq("/topic/user/worker"), any(Map.class));
        verify(operations).acknowledge("stream:application-status", "application-status-notifications", record.getId());
        verify(operations, never()).add(eq("stream:application-status:dlq"), anyMap());
    }

    @Test void invalidPayloadGoesToDlqAndIsAcknowledged() {
        MapRecord<String, String, String> record = MapRecord.create("stream:application-status", Map.of("payload", "invalid")).withId(RecordId.of("2-0"));
        consumer.onMessage(record);
        verify(operations).add(eq("stream:application-status:dlq"), argThat(m -> "2-0".equals(m.get("originalId")) && "3".equals(m.get("retryAttempts"))));
        verify(operations).acknowledge("stream:application-status", "application-status-notifications", record.getId());
        verifyNoInteractions(websocket);
    }
}
