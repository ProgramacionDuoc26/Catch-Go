package com.catchandgo.jobs.messaging.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/** Retencion maxima por stream; la politica se aplica al publicar entradas nuevas. */
@Service
public class StreamRetentionService {
    private static final String KEY = "catchgo:stream:retention";
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

    public void remove(String stream) {
        redis.opsForHash().delete(KEY, stream);
    }
}
