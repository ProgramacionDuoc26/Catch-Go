package com.catchandgo.notification.messaging.consumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class EventDeliveryGuardRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    static LettuceConnectionFactory factory;
    static EventDeliveryGuard guard;

    @BeforeAll static void connect() {
        factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        var template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        guard = new EventDeliveryGuard(template);
    }
    @AfterAll static void close() { if (factory != null) factory.destroy(); }

    @Test void claimsAreExclusiveCompletedEventsAreDeduplicatedAndGroupsAreIndependent() {
        var first = guard.acquire("group-a", "event-1");
        assertEquals(EventDeliveryGuard.State.ACQUIRED, first.state());
        assertEquals(EventDeliveryGuard.State.BUSY, guard.acquire("group-a", "event-1").state());
        guard.complete(first);
        assertEquals(EventDeliveryGuard.State.COMPLETED, guard.acquire("group-a", "event-1").state());
        var otherGroup = guard.acquire("group-b", "event-1");
        assertEquals(EventDeliveryGuard.State.ACQUIRED, otherGroup.state());
        guard.release(otherGroup);
        assertEquals(EventDeliveryGuard.State.ACQUIRED, guard.acquire("group-b", "event-1").state());
        assertThrows(IllegalStateException.class, () -> guard.complete(otherGroup));
    }
}
