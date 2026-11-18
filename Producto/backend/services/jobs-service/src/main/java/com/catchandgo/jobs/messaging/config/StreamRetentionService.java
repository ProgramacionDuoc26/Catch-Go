package com.catchandgo.jobs.messaging.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import java.util.List;

/** Retencion maxima por stream; la politica se aplica al publicar entradas nuevas. */
@Service
public class StreamRetentionService {
    private static final String KEY = "catchgo:stream:retention";
    private static final DefaultRedisScript<Long> SAFE_TRIM = new DefaultRedisScript<>();
    static {
        SAFE_TRIM.setLocation(new ClassPathResource("redis/trim-stream-safely.lua"));
        SAFE_TRIM.setResultType(Long.class);
    }
    private final StringRedisTemplate redis;
    private final MessagingProperties properties;

    public StreamRetentionService(StringRedisTemplate redis, MessagingProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public Long effectiveMaxLen(String stream) {
        Object value = redis.opsForHash().get(KEY, stream);
        return value == null ? properties.getRetention().getMaxLen() : Long.valueOf(value.toString());
    }

    public void setMaxLen(String stream, long maxLen) {
        if (maxLen <= 0) throw new IllegalArgumentException("maxLen debe ser positivo");
        redis.opsForHash().put(KEY, stream, Long.toString(maxLen));
    }

    public long trimSafely(String stream, long maxLen) {
        if (maxLen <= 0) throw new IllegalArgumentException("maxLen debe ser positivo");
        Long removed = redis.execute(SAFE_TRIM, List.of(stream), Long.toString(maxLen));
        if (removed == null) throw new IllegalStateException("Redis no confirmo la retencion");
        return removed;
    }

    public void remove(String stream) {
        redis.opsForHash().delete(KEY, stream);
    }
}
