package com.catchandgo.jobs.messaging.config;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
class SafeStreamRetentionRedisIntegrationTest {
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
    static LettuceConnectionFactory factory;
    static StringRedisTemplate template;
    static StreamRetentionService retention;

    @BeforeAll static void connect() {
        factory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379)));
        factory.afterPropertiesSet();
        factory.start();
        template = new StringRedisTemplate(factory);
        template.afterPropertiesSet();
        retention = new StreamRetentionService(template, new MessagingProperties());
    }
    @AfterAll static void close() { if (factory != null) factory.destroy(); }

    private RecordId add(String stream, String id) {
        return template.opsForStream().add(MapRecord.create(stream, Map.of("payload", id)).withId(RecordId.of(id)));
    }

    @Test void noGroupOrUnreadMessagesAreNotTrimmed() {
        String stream = "stream:retention-unread";
        add(stream, "1-0"); add(stream, "2-0"); add(stream, "3-0");
        assertEquals(0, retention.trimSafely(stream, 1));
        template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), "workers");
        assertEquals(0, retention.trimSafely(stream, 1));
        assertEquals(3L, template.opsForStream().size(stream));
    }

    @Test void pendingAndUnreadEntriesStayUntilAllExistingGroupsProgress() {
        String stream = "stream:retention-pending";
        var first = add(stream, "1-0"); var second = add(stream, "2-0"); var third = add(stream, "3-0");
        template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), "workers");
        template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), "audit");
        template.opsForStream().read(Consumer.from("workers", "one"), StreamReadOptions.empty().count(2),
                StreamOffset.create(stream, ReadOffset.lastConsumed()));
        template.opsForStream().acknowledge(stream, "workers", first);
        assertEquals(0, retention.trimSafely(stream, 1)); // audit aun no leyo nada
        template.opsForStream().read(Consumer.from("audit", "two"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
        template.opsForStream().acknowledge(stream, "audit", first, second, third);
        assertEquals(1, retention.trimSafely(stream, 1)); // conserva el pendiente 2-0 y el no leido 3-0
        assertEquals(List.of("2-0", "3-0"), template.opsForStream().range(stream, Range.unbounded()).stream().map(r -> r.getId().getValue()).toList());
        template.opsForStream().acknowledge(stream, "workers", second);
        template.opsForStream().read(Consumer.from("workers", "one"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
        template.opsForStream().acknowledge(stream, "workers", third);
        assertEquals(1, retention.trimSafely(stream, 1));
        assertEquals(1L, template.opsForStream().size(stream));
    }

    @Test void comparesLargeStreamIdsWithoutFloatingPointRounding() {
        String stream = "stream:retention-large-ids";
        var first = add(stream, "9007199254740993-1");
        var second = add(stream, "9007199254740993-2");
        template.opsForStream().createGroup(stream, ReadOffset.from("0-0"), "workers");
        template.opsForStream().read(Consumer.from("workers", "one"), StreamOffset.create(stream, ReadOffset.lastConsumed()));
        template.opsForStream().acknowledge(stream, "workers", first, second);
        assertEquals(1, retention.trimSafely(stream, 1));
        assertEquals(second, template.opsForStream().range(stream, Range.unbounded()).get(0).getId());
    }
}
