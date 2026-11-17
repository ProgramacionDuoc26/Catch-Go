package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class JobApplicationStreamConsumerTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final StreamOperations<String, String, String> streams = mock(StreamOperations.class);
    private final SimpMessagingTemplate websocket = mock(SimpMessagingTemplate.class);
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final MessagingProperties properties = new MessagingProperties();
    private final EventDeliveryGuard guard = mock(EventDeliveryGuard.class);
    private JobApplicationStreamConsumer consumer;
    private static final String STREAM = "stream:job-applications";
    private static final String GROUP = "notifications-group";
    private static final RecordId ID = RecordId.of("1710000000000-1");

    @BeforeEach void setup() {
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(guard.acquire(anyString(), anyString())).thenReturn(
                new EventDeliveryGuard.Claim(EventDeliveryGuard.State.ACQUIRED, "key", "token"));
        consumer = new JobApplicationStreamConsumer(redis, websocket, mapper, properties, guard);
    }
    private MapRecord<String, String, String> record(String payload) {
        return MapRecord.create(STREAM, Map.of("payload", payload)).withId(ID);
    }
    private PendingMessages pending(long deliveries) {
        return new PendingMessages(GROUP, List.of(new PendingMessage(ID,
                Consumer.from(GROUP, "old-consumer"), Duration.ofSeconds(70), deliveries)));
    }

    @Test void validEventIsDeliveredAndAcknowledged() throws Exception {
        var event = JobApplicationEvent.create(10L, 1L, "Offer", "worker", "Worker", "", "company");
        consumer.onMessage(record(mapper.writeValueAsString(event)));
        verify(websocket).convertAndSend(eq("/topic/user/company"), argThat((Map<String,String> m) -> "info".equals(m.get("type"))));
        verify(streams).acknowledge(STREAM, GROUP, ID);
    }

    @Test void failedEventRemainsPendingBeforeMaximum() {
        when(streams.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(1L))).thenReturn(pending(1));
        consumer.onMessage(record("invalid"));
        verify(streams, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
        verify(streams, never()).add(eq("stream:job-applications:dlq"), anyMap());
    }

    @Test void lastFailedDeliveryGoesToDlqBeforeAck() {
        when(streams.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(1L))).thenReturn(pending(3));
        when(streams.add(eq("stream:job-applications:dlq"), anyMap())).thenReturn(RecordId.of("2-0"));
        consumer.onMessage(record("invalid"));
        var order = inOrder(streams);
        order.verify(streams).add(eq("stream:job-applications:dlq"), argThat(m -> "3".equals(m.get("retryAttempts"))));
        order.verify(streams).acknowledge(STREAM, GROUP, ID);
    }

    @Test void failedDlqWriteDoesNotAcknowledge() {
        when(streams.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(1L))).thenReturn(pending(3));
        when(streams.add(eq("stream:job-applications:dlq"), anyMap())).thenThrow(new RuntimeException("Redis unavailable"));
        consumer.onMessage(record("invalid"));
        verify(streams, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
    }

    @Test void recoveryClaimsAbandonedMessage() throws Exception {
        var event = JobApplicationEvent.create(10L, 1L, "Offer", "worker", "Worker", "", "company");
        when(streams.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(100L))).thenReturn(pending(1));
        when(streams.claim(eq(STREAM), eq(GROUP), anyString(), eq(Duration.ofSeconds(60)), eq(ID)))
                .thenReturn(List.of(record(mapper.writeValueAsString(event))));
        consumer.recoverPending();
        verify(websocket).convertAndSend(eq("/topic/user/company"), any(Map.class));
        verify(streams).acknowledge(STREAM, GROUP, ID);
    }
    @Test void previouslyCompletedEventIsAcknowledgedWithoutSendingAgain() throws Exception {
        var event = JobApplicationEvent.create(10L, 1L, "Offer", "worker", "Worker", "", "company");
        when(guard.acquire(GROUP, event.eventId())).thenReturn(
                new EventDeliveryGuard.Claim(EventDeliveryGuard.State.COMPLETED, "key", "token"));
        consumer.onMessage(record(mapper.writeValueAsString(event)));
        verifyNoInteractions(websocket);
        verify(streams).acknowledge(STREAM, GROUP, ID);
    }

    @Test void busyEventRemainsPending() throws Exception {
        var event = JobApplicationEvent.create(10L, 1L, "Offer", "worker", "Worker", "", "company");
        when(guard.acquire(GROUP, event.eventId())).thenReturn(
                new EventDeliveryGuard.Claim(EventDeliveryGuard.State.BUSY, "key", "token"));
        consumer.onMessage(record(mapper.writeValueAsString(event)));
        verifyNoInteractions(websocket);
        verify(streams, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
    }

    @Test void confirmationFailureLeavesEventPending() throws Exception {
        var event = JobApplicationEvent.create(10L, 1L, "Offer", "worker", "Worker", "", "company");
        doThrow(new IllegalStateException("Redis unavailable")).when(guard).complete(any());
        consumer.onMessage(record(mapper.writeValueAsString(event)));
        verify(websocket).convertAndSend(eq("/topic/user/company"), any(Map.class));
        verify(streams, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
    }
}
