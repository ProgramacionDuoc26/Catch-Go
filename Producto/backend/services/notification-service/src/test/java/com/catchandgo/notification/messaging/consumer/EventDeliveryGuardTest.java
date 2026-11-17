package com.catchandgo.notification.messaging.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EventDeliveryGuardTest {
    @Test void acquireReportsNewCompletedAndBusyStates() {
        var redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString())).thenReturn(1L, 2L, 0L);
        var guard = new EventDeliveryGuard(redis);
        assertEquals(EventDeliveryGuard.State.ACQUIRED, guard.acquire("group", "event").state());
        assertEquals(EventDeliveryGuard.State.COMPLETED, guard.acquire("group", "event").state());
        assertEquals(EventDeliveryGuard.State.BUSY, guard.acquire("group", "event").state());
    }
    @Test void completionMustConfirmTheLeaseIsStillOwned() {
        var redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(RedisScript.class), anyList(), anyString(), anyString())).thenReturn(0L);
        var guard = new EventDeliveryGuard(redis);
        var claim = new EventDeliveryGuard.Claim(EventDeliveryGuard.State.ACQUIRED, "key", "token");
        assertThrows(IllegalStateException.class, () -> guard.complete(claim));
    }
}
