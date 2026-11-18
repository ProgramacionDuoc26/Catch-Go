package com.catchandgo.jobs.messaging.config;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class StreamConfigRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test void producerInitializationPreservesEarlierEventsForBothGroups() throws Exception {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        try {
            var template = new StringRedisTemplate(factory);
            template.afterPropertiesSet();
            var applications = template.opsForStream().add("stream:job-applications", Map.of("payload", "first"));
            var statuses = template.opsForStream().add("stream:application-status", Map.of("payload", "second"));
            var config = new StreamConfig(new MessagingProperties());
            config.initRedisStreams(template).run();
            var first = template.opsForStream().read(Consumer.from("notifications-group", "consumer"),
                    StreamOffset.create("stream:job-applications", ReadOffset.lastConsumed()));
            var second = template.opsForStream().read(Consumer.from("application-status-notifications", "consumer"),
                    StreamOffset.create("stream:application-status", ReadOffset.lastConsumed()));
            assertNotNull(first);
            assertNotNull(second);
            assertEquals(applications, first.get(0).getId());
            assertEquals(statuses, second.get(0).getId());
            template.opsForStream().acknowledge("stream:job-applications", "notifications-group", applications);
            template.opsForStream().acknowledge("stream:application-status", "application-status-notifications", statuses);
            config.initRedisStreams(template).run();
            var afterRestart = template.opsForStream().read(Consumer.from("notifications-group", "consumer"),
                    StreamOffset.create("stream:job-applications", ReadOffset.lastConsumed()));
            assertTrue(afterRestart == null || afterRestart.isEmpty());
        } finally {
            factory.destroy();
        }
    }
}
