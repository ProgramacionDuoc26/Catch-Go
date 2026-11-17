package com.catchandgo.notification.messaging.consumer;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

/** Deduplica entregas conocidas; no garantiza entrega exactamente una vez a WebSocket. */
@Component
public class EventDeliveryGuard {
    private static final Duration LEASE = Duration.ofMinutes(2);
    private static final Duration COMPLETED_RETENTION = Duration.ofDays(7);
    private static final DefaultRedisScript<Long> ACQUIRE = new DefaultRedisScript<>(
        "local v=redis.call('GET',KEYS[1]); if v=='done' then return 2 end; " +
        "if v then return 0 end; redis.call('SET',KEYS[1],ARGV[1],'PX',ARGV[2]); return 1", Long.class);
    private static final DefaultRedisScript<Long> COMPLETE = new DefaultRedisScript<>(
        "if redis.call('GET',KEYS[1])==ARGV[1] then " +
        "redis.call('SET',KEYS[1],'done','PX',ARGV[2]); return 1 end; return 0", Long.class);
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
        "if redis.call('GET',KEYS[1])==ARGV[1] then return redis.call('DEL',KEYS[1]) end; return 0", Long.class);
    private final StringRedisTemplate redis;

    public EventDeliveryGuard(StringRedisTemplate redis) { this.redis = redis; }
    public enum State { ACQUIRED, COMPLETED, BUSY }
    public record Claim(State state, String key, String token) {}

    public Claim acquire(String group, String eventId) {
        String key = "catchgo:notification:dedup:" + group + ":" + eventId;
        String token = UUID.randomUUID().toString();
        Long result = redis.execute(ACQUIRE, List.of(key), token, Long.toString(LEASE.toMillis()));
        if (result == null) throw new IllegalStateException("Redis no confirmo la reserva del evento");
        if (result < 0 || result > 2) throw new IllegalStateException("Respuesta inesperada de Redis");
        State state = result == 1 ? State.ACQUIRED : result == 2 ? State.COMPLETED : State.BUSY;
        return new Claim(state, key, token);
    }

    public void complete(Claim claim) {
        Long result = redis.execute(COMPLETE, List.of(claim.key()), claim.token(), Long.toString(COMPLETED_RETENTION.toMillis()));
        if (!Long.valueOf(1).equals(result)) throw new IllegalStateException("Se perdio la reserva del evento");
    }

    public void release(Claim claim) {
        redis.execute(RELEASE, List.of(claim.key()), claim.token());
    }
}
