package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.notification.messaging.config.MessagingProperties;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PendingRecoveryTest {
    private static final String STREAM = "stream:test";
    private static final String GROUP = "test-group";
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final StreamOperations<String, String, String> operations = mock(StreamOperations.class);
    private final MessagingProperties properties = new MessagingProperties();

    private PendingMessages page(int start, int size, Duration idle) {
        List<PendingMessage> items = IntStream.range(start, start + size).mapToObj(i -> new PendingMessage(
                RecordId.of(i + "-0"), Consumer.from(GROUP, "old"), idle, 1)).toList();
        return new PendingMessages(GROUP, items);
    }

    @Test void examinesOlderEntriesBeyondFirstHundred() {
        when(redis.<String, String>opsForStream()).thenReturn(operations);
        when(operations.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(100L)))
                .thenReturn(page(1, 100, Duration.ZERO), page(101, 1, Duration.ofSeconds(70)));
        var id = RecordId.of("101-0");
        var record = MapRecord.create(STREAM, Map.of("payload", "event")).withId(id);
        when(operations.claim(eq(STREAM), eq(GROUP), anyString(), eq(Duration.ofSeconds(60)), eq(id)))
                .thenReturn(List.of(record));
        @SuppressWarnings("unchecked") var handler = (java.util.function.Consumer<MapRecord<String, String, String>>) mock(java.util.function.Consumer.class);
        new PendingRecovery(redis, properties).recover(STREAM, GROUP, handler);
        verify(handler).accept(record);
        @SuppressWarnings("rawtypes") ArgumentCaptor<Range> ranges = ArgumentCaptor.forClass(Range.class);
        verify(operations, times(2)).pending(eq(STREAM), eq(GROUP), ranges.capture(), eq(100L));
        assertEquals("100-0", ranges.getAllValues().get(1).getLowerBound().getValue().orElseThrow());
        assertFalse(ranges.getAllValues().get(1).getLowerBound().isInclusive());
    }

    @Test void continuesAfterThousandEntriesOnNextCycle() {
        when(redis.<String, String>opsForStream()).thenReturn(operations);
        AtomicInteger requests = new AtomicInteger();
        when(operations.pending(eq(STREAM), eq(GROUP), any(Range.class), eq(100L)))
                .thenAnswer(invocation -> page(requests.getAndIncrement() * 100 + 1, 100, Duration.ZERO));
        var recovery = new PendingRecovery(redis, properties);
        recovery.recover(STREAM, GROUP, ignored -> {});
        verify(operations, times(10)).pending(eq(STREAM), eq(GROUP), any(Range.class), eq(100L));
        recovery.recover(STREAM, GROUP, ignored -> {});
        @SuppressWarnings("rawtypes") ArgumentCaptor<Range> ranges = ArgumentCaptor.forClass(Range.class);
        verify(operations, times(20)).pending(eq(STREAM), eq(GROUP), ranges.capture(), eq(100L));
        assertEquals("1000-0", ranges.getAllValues().get(10).getLowerBound().getValue().orElseThrow());
        assertFalse(ranges.getAllValues().get(10).getLowerBound().isInclusive());
    }
}
