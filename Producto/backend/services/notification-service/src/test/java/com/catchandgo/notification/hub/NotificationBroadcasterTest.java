package com.catchandgo.notification.hub;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationBroadcasterTest {
    private final StringRedisTemplate redis = mock(StringRedisTemplate.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final NotificationBroadcaster broadcaster = new NotificationBroadcaster(redis, mapper);

    @Test void publishesJsonToSharedChannel() throws Exception {
        when(redis.convertAndSend(eq("notifications"), anyString())).thenReturn(2L);
        var notification = Map.of("userId", "123", "message", "Mensaje");
        broadcaster.publish(notification);
        verify(redis).convertAndSend(eq("notifications"), argThat(payload -> {
            try { return mapper.readTree((String) payload).get("userId").asText().equals("123"); }
            catch (Exception e) { return false; }
        }));
    }

    @Test void noListenersIsARecoverableFailure() {
        when(redis.convertAndSend(eq("notifications"), anyString())).thenReturn(0L);
        assertThrows(IllegalStateException.class, () -> broadcaster.publish(Map.of("userId", "123")));
    }

    @Test void redisFailureIsNotHidden() {
        when(redis.convertAndSend(eq("notifications"), anyString())).thenThrow(new IllegalStateException("Redis unavailable"));
        assertThrows(IllegalStateException.class, () -> broadcaster.publish(Map.of("userId", "123")));
    }
}
