package com.catchandgo.notification.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StreamConsumerConfigTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final StreamOperations<String, String, String> streams = mock(StreamOperations.class);
    private final StreamConsumerConfig config = new StreamConsumerConfig(new MessagingProperties());

    @Test void instancesReceiveDistinctConsumerNamesAndMayBeConfigured() {
        var first = new MessagingProperties();
        var second = new MessagingProperties();
        assertTrue(first.getConsumer().getName().startsWith("notification-consumer-"));
        assertNotEquals(first.getConsumer().getName(), second.getConsumer().getName());
        first.getConsumer().setName("notification-consumer-custom");
        assertEquals("notification-consumer-custom", first.getConsumer().getName());
    }

    @Test void newGroupsReadFromBeginningForBothStreams() {
        when(redis.hasKey(anyString())).thenReturn(true);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        config.ensureStreamAndGroup(redis, "stream:job-applications", "notifications-group");
        config.ensureStreamAndGroup(redis, "stream:application-status", "application-status-notifications");
        verify(streams).createGroup(eq("stream:job-applications"), argThat(offset -> "0-0".equals(offset.getOffset())), eq("notifications-group"));
        verify(streams).createGroup(eq("stream:application-status"), argThat(offset -> "0-0".equals(offset.getOffset())), eq("application-status-notifications"));
    }

    @Test void existingGroupKeepsItsPosition() {
        when(redis.hasKey(anyString())).thenReturn(true);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(streams.createGroup(anyString(), any(ReadOffset.class), anyString()))
                .thenThrow(new IllegalStateException("wrapper", new RuntimeException("BUSYGROUP group already exists")));
        assertDoesNotThrow(() -> config.ensureStreamAndGroup(redis, "stream:job-applications", "notifications-group"));
    }

    @Test void unavailableRedisDoesNotPretendGroupExists() {
        when(redis.hasKey(anyString())).thenReturn(true);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(streams.createGroup(anyString(), any(ReadOffset.class), anyString()))
                .thenThrow(new IllegalStateException("Redis unavailable"));
        assertThrows(IllegalStateException.class,
                () -> config.ensureStreamAndGroup(redis, "stream:job-applications", "notifications-group"));
    }

    @Test void initializesMissingStreamAndRejectsUnconfirmedWrite() {
        when(redis.hasKey(anyString())).thenReturn(false);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(streams.add(anyString(), anyMap())).thenReturn(RecordId.of("1-0"), null);
        config.ensureStreamAndGroup(redis, "stream:first", "group");
        verify(streams).add("stream:first", Map.of("system", "init"));
        assertThrows(IllegalStateException.class, () -> config.ensureStreamAndGroup(redis, "stream:second", "group"));
        verify(streams, never()).createGroup(eq("stream:second"), any(ReadOffset.class), anyString());
    }
}
