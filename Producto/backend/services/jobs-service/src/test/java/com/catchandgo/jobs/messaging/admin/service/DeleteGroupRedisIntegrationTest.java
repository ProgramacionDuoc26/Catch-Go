package com.catchandgo.jobs.messaging.admin.service;

import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.catchandgo.jobs.messaging.config.StreamRetentionService;
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
class DeleteGroupRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Test void groupCanOnlyBeDeletedAfterItsWorkAndConsumersAreCleared() {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        try {
            var template = new StringRedisTemplate(factory);
            template.afterPropertiesSet();
            var properties = new MessagingProperties();
            var service = new QueueAdminService(template, properties, new StreamRetentionService(template, properties));
            String stream = "stream:demo-group-deletion";
            String group = "temporary";
            var id = template.opsForStream().add(stream, Map.of("payload", "event"));
            template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), group);
            assertEquals(QueueAdminService.GroupDeletion.UNREAD, service.deleteGroup(stream, group));
            template.opsForStream().read(Consumer.from(group, "worker"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertEquals(QueueAdminService.GroupDeletion.PENDING, service.deleteGroup(stream, group));
            template.opsForStream().acknowledge(stream, group, id);
            assertEquals(QueueAdminService.GroupDeletion.CONSUMERS, service.deleteGroup(stream, group));
            template.opsForStream().deleteConsumer(stream, Consumer.from(group, "worker"));
            assertEquals(QueueAdminService.GroupDeletion.DELETED, service.deleteGroup(stream, group));
            assertEquals(QueueAdminService.GroupDeletion.NOT_FOUND, service.deleteGroup(stream, group));
        } finally {
            factory.destroy();
        }
    }

    @Test void replayRestartsReadingOnlyAfterPendingAndConsumersAreCleared() {
        var factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        try {
            var template = new StringRedisTemplate(factory);
            template.afterPropertiesSet();
            var properties = new MessagingProperties();
            var service = new QueueAdminService(template, properties, new StreamRetentionService(template, properties));
            String stream = "stream:demo-group-replay";
            String group = "temporary";
            var id = template.opsForStream().add(stream, Map.of("payload", "event"));
            template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), group);
            template.opsForStream().read(Consumer.from(group, "worker"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertEquals(QueueAdminService.GroupReplay.PENDING, service.replayGroup(stream, group));
            template.opsForStream().acknowledge(stream, group, id);
            assertEquals(QueueAdminService.GroupReplay.CONSUMERS, service.replayGroup(stream, group));
            template.opsForStream().deleteConsumer(stream, Consumer.from(group, "worker"));
            assertEquals(QueueAdminService.GroupReplay.RESET, service.replayGroup(stream, group));
            var replayed = template.opsForStream().read(Consumer.from(group, "worker"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
            assertNotNull(replayed);
            assertEquals(id, replayed.get(0).getId());
        } finally {
            factory.destroy();
        }
    }
}
