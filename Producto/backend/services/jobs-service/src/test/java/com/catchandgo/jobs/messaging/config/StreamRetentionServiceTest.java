package com.catchandgo.jobs.messaging.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
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
}
