package com.catchandgo.notification.config;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.notification.hub.NotificationBroadcaster;
import com.catchandgo.notification.hub.NotificationHandler;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.catchandgo.notification.messaging.config.StreamConsumerConfig;
import com.catchandgo.notification.messaging.consumer.ApplicationStatusStreamConsumer;
import com.catchandgo.notification.messaging.consumer.EventDeliveryGuard;
import com.catchandgo.notification.messaging.consumer.JobApplicationStreamConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
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
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Testcontainers(disabledWithoutDocker = true)
class StreamFlowRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test void bothStreamsDeliverNotificationsAndAcknowledgeEvents() throws Exception {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        var template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        var mapper = new ObjectMapper().findAndRegisterModules();
        var websocket = mock(SimpMessagingTemplate.class);
        var delivered = new CountDownLatch(2);
        doAnswer(invocation -> { delivered.countDown(); return null; })
                .when(websocket).convertAndSend(anyString(), any(Object.class));
        var listenerConfig = new RedisListenerConfig();
        var adapter = listenerConfig.listenerAdapter(new NotificationHandler(websocket, mapper));
        adapter.afterPropertiesSet();
        var listener = listenerConfig.container(factory, adapter);
        var properties = new MessagingProperties();
        properties.getConsumer().setPollTimeoutMs(100);
        var guard = new EventDeliveryGuard(template);
        var broadcaster = new NotificationBroadcaster(template, mapper);
        var applications = new JobApplicationStreamConsumer(template, broadcaster, mapper, properties, guard);
        var statuses = new ApplicationStatusStreamConsumer(template, broadcaster, mapper, properties, guard);
        var appEvent = JobApplicationEvent.create(10L, 1L, "Oferta", "worker", "Worker", "", "company");
        var statusEvent = ApplicationStatusEvent.create(10L, 1L, "worker", "PENDIENTE", "ACEPTADO", "Aceptada", "Seleccionado", "success");
        var appStream = "stream:job-applications";
        var statusStream = "stream:application-status";
        var appGroup = "notifications-group";
        var statusGroup = "application-status-notifications";
        org.springframework.data.redis.stream.StreamMessageListenerContainer<?, ?> streamListener = null;
        try {
            listener.afterPropertiesSet();
            listener.start();
            template.opsForStream().add(appStream, Map.of("payload", mapper.writeValueAsString(appEvent)));
            template.opsForStream().add(statusStream, Map.of("payload", mapper.writeValueAsString(statusEvent)));
            streamListener = new StreamConsumerConfig(properties).streamMessageListenerContainer(factory, template, applications, statuses);

            assertTrue(delivered.await(15, TimeUnit.SECONDS), "No llegaron las dos notificaciones a WebSocket");
            await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
                assertEquals(0, template.opsForStream().pending(appStream, appGroup).getTotalPendingMessages());
                assertEquals(0, template.opsForStream().pending(statusStream, statusGroup).getTotalPendingMessages());
                assertEquals("done", template.opsForValue().get("catchgo:notification:dedup:" + appGroup + ":" + appEvent.eventId()));
                assertEquals("done", template.opsForValue().get("catchgo:notification:dedup:" + statusGroup + ":" + statusEvent.eventId()));
            });
            verify(websocket, times(1)).convertAndSend(eq("/topic/user/company"), argThat((Object payload) ->
                    payload instanceof Map<?, ?> data && appEvent.eventId().equals(data.get("eventId"))));
            verify(websocket, times(1)).convertAndSend(eq("/topic/user/worker"), argThat((Object payload) ->
                    payload instanceof Map<?, ?> data && statusEvent.eventId().equals(data.get("eventId"))));
        } finally {
            if (streamListener != null) streamListener.stop();
            listener.destroy();
            factory.destroy();
        }
    }
}
