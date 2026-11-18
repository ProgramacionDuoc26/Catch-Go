package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Map;
import java.util.List;
import java.time.Duration;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ApplicationStatusStreamConsumerTest {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, String, String> operations = mock(StreamOperations.class);
    SimpMessagingTemplate websocket = mock(SimpMessagingTemplate.class);
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    MessagingProperties properties = new MessagingProperties();
    EventDeliveryGuard guard = mock(EventDeliveryGuard.class);
    ApplicationStatusStreamConsumer consumer;

    @BeforeEach void setup() {
        when(redis.<String, String>opsForStream()).thenReturn(operations);
        when(guard.acquire(anyString(), anyString())).thenReturn(
                new EventDeliveryGuard.Claim(EventDeliveryGuard.State.ACQUIRED, "key", "token"));
        consumer = new ApplicationStatusStreamConsumer(redis, websocket, mapper, properties, guard);
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
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenReturn(RecordId.of("3-0"));
        consumer.onMessage(record);
        verify(operations).add(eq("stream:application-status:dlq"), argThat(m -> "2-0".equals(m.get("originalId")) && "1".equals(m.get("retryAttempts"))));
        verify(operations).acknowledge("stream:application-status", "application-status-notifications", record.getId());
        verifyNoInteractions(websocket);
    }

    @ParameterizedTest
    @ValueSource(strings = {"applicationId", "jobId", "workerId", "newStatus", "title", "message", "notificationType", "eventId", "occurredAt"})
    void missingRequiredFieldGoesToDlq(String field) throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        ObjectNode json = mapper.valueToTree(event);
        json.remove(field);
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(json))).withId(RecordId.of("2-0"));
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenReturn(RecordId.of("3-0"));
        consumer.onMessage(record);
        verify(operations).add(eq("stream:application-status:dlq"), anyMap());
        verify(operations).acknowledge(record.getStream(), "application-status-notifications", record.getId());
        verifyNoInteractions(websocket);
        verify(guard, never()).acquire(anyString(), anyString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"title", "message", "newStatus", "notificationType"})
    void blankTextGoesToDlq(String field) throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        ObjectNode json = mapper.valueToTree(event);
        json.put(field, "  ");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(json))).withId(RecordId.of("2-0"));
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenReturn(RecordId.of("3-0"));
        consumer.onMessage(record);
        verify(operations).add(eq("stream:application-status:dlq"), anyMap());
        verifyNoInteractions(websocket);
    }

    @Test void unknownTypeAndNonpositiveIdsAreRejected() throws Exception {
        var event = ApplicationStatusEvent.create(0L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var invalidType = ApplicationStatusEvent.create(1L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "unknown");
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenReturn(RecordId.of("3-0"));
        for (var invalid : List.of(event, invalidType)) {
            consumer.onMessage(MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(invalid))).withId(RecordId.of("2-0")));
        }
        verify(operations, times(2)).add(eq("stream:application-status:dlq"), anyMap());
        verifyNoInteractions(websocket);
    }

    @Test void previousStatusMayBeAbsent() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", null, "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("1-0"));
        consumer.onMessage(record);
        verify(websocket).convertAndSend(eq("/topic/user/worker"), any(Map.class));
        verify(operations).acknowledge(record.getStream(), "application-status-notifications", record.getId());
    }
    @Test void completedEventIsAcknowledgedWithoutSendingAgain() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        when(guard.acquire("application-status-notifications", event.eventId())).thenReturn(
                new EventDeliveryGuard.Claim(EventDeliveryGuard.State.COMPLETED, "key", "token"));
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("1-0"));
        consumer.onMessage(record);
        verifyNoInteractions(websocket);
        verify(operations).acknowledge("stream:application-status", "application-status-notifications", record.getId());
    }

    @Test void confirmationFailureDoesNotMoveSentEventToDlq() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("1-0"));
        doThrow(new IllegalStateException("Redis unavailable")).when(guard).complete(any());
        consumer.onMessage(record);
        verify(operations, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
        verify(operations, never()).add(eq("stream:application-status:dlq"), anyMap());
    }

    private PendingMessages pending(RecordId id, long deliveries) {
        return new PendingMessages("application-status-notifications", List.of(new PendingMessage(id,
                Consumer.from("application-status-notifications", "consumer"), Duration.ofSeconds(70), deliveries)));
    }

    @Test void failedDeliveryRemainsPendingUntilNextRecovery() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("4-0"));
        when(operations.pending(eq(record.getStream()), anyString(), any(Range.class), eq(1L))).thenReturn(pending(record.getId(), 1));
        doThrow(new IllegalStateException("Temporal")).when(websocket).convertAndSend(anyString(), any(Map.class));
        consumer.onMessage(record);
        verify(websocket, times(1)).convertAndSend(anyString(), any(Map.class));
        verify(guard).release(any());
        verify(operations, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
        verify(operations, never()).add(anyString(), anyMap());
    }

    @Test void lastFailedDeliveryWritesDlqBeforeAck() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("4-0"));
        when(operations.pending(eq(record.getStream()), anyString(), any(Range.class), eq(1L))).thenReturn(pending(record.getId(), 3));
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenReturn(RecordId.of("5-0"));
        doThrow(new IllegalStateException("Temporal")).when(websocket).convertAndSend(anyString(), any(Map.class));
        consumer.onMessage(record);
        var order = inOrder(operations);
        order.verify(operations).add(eq("stream:application-status:dlq"), argThat(m -> "3".equals(m.get("retryAttempts"))));
        order.verify(operations).acknowledge(record.getStream(), "application-status-notifications", record.getId());
    }

    @Test void dlqFailureKeepsMessagePending() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("4-0"));
        when(operations.pending(eq(record.getStream()), anyString(), any(Range.class), eq(1L))).thenReturn(pending(record.getId(), 3));
        when(operations.add(eq("stream:application-status:dlq"), anyMap())).thenThrow(new IllegalStateException("Redis unavailable"));
        doThrow(new IllegalStateException("Temporal")).when(websocket).convertAndSend(anyString(), any(Map.class));
        consumer.onMessage(record);
        verify(operations, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
    }

    @Test void recoveryClaimsAndProcessesPendingMessage() throws Exception {
        var event = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var record = MapRecord.create("stream:application-status", Map.of("payload", mapper.writeValueAsString(event))).withId(RecordId.of("4-0"));
        when(operations.pending(eq(record.getStream()), anyString(), any(Range.class), eq(100L))).thenReturn(pending(record.getId(), 1));
        when(operations.claim(eq(record.getStream()), anyString(), anyString(), eq(Duration.ofSeconds(60)), eq(record.getId())))
                .thenReturn(List.of(record));
        consumer.recoverPending();
        verify(websocket).convertAndSend(eq("/topic/user/worker"), any(Map.class));
        verify(operations).acknowledge(record.getStream(), "application-status-notifications", record.getId());
    }
}
