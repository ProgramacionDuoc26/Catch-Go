package com.catchandgo.notification.messaging.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class StreamConsumerConfigRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    static LettuceConnectionFactory factory;
    static StringRedisTemplate template;

    @BeforeAll static void connect() {
        factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
    }

    @AfterAll static void close() { if (factory != null) factory.destroy(); }

    @Test void newGroupsReadExistingEventsAndRestartDoesNotResetProgress() {
        var config = new StreamConsumerConfig(new MessagingProperties());
        for (String stream : new String[]{"stream:job-applications", "stream:application-status"}) {
            String group = stream + "-group";
            var originalId = template.opsForStream().add(stream, Map.of("payload", "before-group"));
            config.ensureStreamAndGroup(template, stream, group);
            var first = template.opsForStream().read(Consumer.from(group, "consumer-1"),
                    StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertNotNull(first);
            assertEquals(1, first.size());
            assertEquals(originalId, first.get(0).getId());
            template.opsForStream().acknowledge(stream, group, originalId);

            config.ensureStreamAndGroup(template, stream, group);
            var afterRestart = template.opsForStream().read(Consumer.from(group, "consumer-1"),
                    StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertTrue(afterRestart == null || afterRestart.isEmpty());
            var nextId = template.opsForStream().add(stream, Map.of("payload", "after-group"));
            var next = template.opsForStream().read(Consumer.from(group, "consumer-1"),
                    StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertNotNull(next);
            assertEquals(1, next.size());
            assertEquals(nextId, next.get(0).getId());
        }
    }
}
