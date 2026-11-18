package com.catchandgo.jobs.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StreamRetentionServiceTest {
    @SuppressWarnings("unchecked")
    @Test void storesAndReadsStreamSpecificPolicy() {
        var redis = mock(StringRedisTemplate.class);
        HashOperations<String, Object, Object> hash = mock(HashOperations.class);
        when(redis.opsForHash()).thenReturn(hash);
        var properties = new MessagingProperties();
        var service = new StreamRetentionService(redis, properties);
        assertEquals(10000L, service.effectiveMaxLen("stream:jobs"));
        service.setMaxLen("stream:jobs", 250);
        verify(hash).put("catchgo:stream:retention", "stream:jobs", "250");
        when(hash.get("catchgo:stream:retention", "stream:jobs")).thenReturn("250");
        assertEquals(250L, service.effectiveMaxLen("stream:jobs"));
        assertThrows(IllegalArgumentException.class, () -> service.setMaxLen("stream:jobs", 0));
    }

    @Test void failsClosedWhenRedisDoesNotConfirmTrim() {
        var redis = mock(StringRedisTemplate.class);
        var service = new StreamRetentionService(redis, new MessagingProperties());
        when(redis.execute(any(RedisScript.class), eq(List.of("stream:jobs")), eq("5"))).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.trimSafely("stream:jobs", 5));
        assertThrows(IllegalArgumentException.class, () -> service.trimSafely("stream:jobs", 0));
    }
}
