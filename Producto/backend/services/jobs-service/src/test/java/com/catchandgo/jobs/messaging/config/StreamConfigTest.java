package com.catchandgo.jobs.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StreamConfigTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    private final StreamOperations<String, String, String> streams = mock(StreamOperations.class);
    private final StreamConfig config = new StreamConfig(new MessagingProperties());

    @Test void initializesBothGroupsFromBeginning() throws Exception {
        when(redis.hasKey(anyString())).thenReturn(true);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        config.initRedisStreams(redis).run();
        verify(streams).createGroup(eq("stream:job-applications"), argThat(offset -> "0-0".equals(offset.getOffset())), eq("notifications-group"));
        verify(streams).createGroup(eq("stream:application-status"), argThat(offset -> "0-0".equals(offset.getOffset())), eq("application-status-notifications"));
    }

    @Test void existingGroupsAreNotResetAndInitializationIsIdempotent() throws Exception {
        when(redis.hasKey(anyString())).thenReturn(true);
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(streams.createGroup(anyString(), any(ReadOffset.class), anyString()))
                .thenThrow(new IllegalStateException("wrapper", new IllegalStateException("BUSYGROUP exists")));
        assertDoesNotThrow(() -> config.initRedisStreams(redis).run());
        verify(streams, times(2)).createGroup(anyString(), any(ReadOffset.class), anyString());
    }

    @Test void redisFailureDoesNotPreventOutboxServiceFromStarting() throws Exception {
        when(redis.hasKey(anyString())).thenThrow(new IllegalStateException("Redis unavailable"));
        assertDoesNotThrow(() -> config.initRedisStreams(redis).run());
        verify(redis, times(4)).hasKey(anyString());
        verifyNoInteractions(streams);
    }
}
