package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.notification.messaging.config.MessagingProperties;
import java.time.Duration;
import java.util.function.Consumer;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Recorre la lista de pendientes por lotes, manteniendo el punto de avance entre ciclos. */
final class PendingRecovery {
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES_PER_CYCLE = 10;
    private static final Duration MIN_IDLE = Duration.ofSeconds(60);
    private final StringRedisTemplate redis;
    private final MessagingProperties properties;
    private String cursor;

    PendingRecovery(StringRedisTemplate redis, MessagingProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    void recover(String stream, String group, Consumer<MapRecord<String, String, String>> handler) {
        for (int page = 0; page < MAX_PAGES_PER_CYCLE; page++) {
            Range<String> range = cursor == null ? Range.unbounded() : Range.rightUnbounded(Range.Bound.exclusive(cursor));
            var pending = redis.opsForStream().pending(stream, group, range, PAGE_SIZE);
            if (pending == null || pending.isEmpty()) {
                cursor = null;
                return;
            }
            for (var message : pending) {
                if (message.getElapsedTimeSinceLastDelivery().compareTo(MIN_IDLE) < 0) continue;
                var claimed = redis.<String, String>opsForStream().claim(stream, group,
                        properties.getConsumer().getName(), MIN_IDLE, message.getId());
                if (claimed != null) claimed.forEach(handler);
            }
            cursor = pending.get(pending.size() - 1).getId().getValue();
            if (pending.size() < PAGE_SIZE) {
                cursor = null;
                return;
            }
        }
    }
}
