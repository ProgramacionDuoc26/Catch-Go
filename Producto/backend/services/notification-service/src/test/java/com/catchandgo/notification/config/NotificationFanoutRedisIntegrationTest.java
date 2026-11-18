package com.catchandgo.notification.config;

import com.catchandgo.notification.hub.NotificationBroadcaster;
import com.catchandgo.notification.hub.NotificationHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
class NotificationFanoutRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test void bothInstancesRelayOnePublicationToTheirLocalWebsockets() throws Exception {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        var template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        var mapper = new ObjectMapper();
        var firstWebsocket = mock(SimpMessagingTemplate.class);
        var secondWebsocket = mock(SimpMessagingTemplate.class);
        var received = new CountDownLatch(2);
        doAnswer(invocation -> { received.countDown(); return null; }).when(firstWebsocket).convertAndSend(anyString(), any(Object.class));
        doAnswer(invocation -> { received.countDown(); return null; }).when(secondWebsocket).convertAndSend(anyString(), any(Object.class));
        var config = new RedisListenerConfig();
        var first = config.container(factory, config.listenerAdapter(new NotificationHandler(firstWebsocket, mapper)));
        var second = config.container(factory, config.listenerAdapter(new NotificationHandler(secondWebsocket, mapper)));
        try {
            first.afterPropertiesSet();
            second.afterPropertiesSet();
            first.start();
            second.start();
            new NotificationBroadcaster(template, mapper).publish(Map.of("userId", "123", "message", "Hola"));
            assertTrue(received.await(5, TimeUnit.SECONDS));
            verify(firstWebsocket, times(1)).convertAndSend(eq("/topic/user/123"), any(Map.class));
            verify(secondWebsocket, times(1)).convertAndSend(eq("/topic/user/123"), any(Map.class));
        } finally {
            first.destroy();
            second.destroy();
            factory.destroy();
        }
    }
}
