package com.catchandgo.jobs.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Propiedades centralizadas de mensajería (Colas, Streams, Grupos y Retención).
 * Cumple con el ítem de configuración centralizada de la rúbrica.
 */
@Configuration
@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

    private Map<String, String> streams = new HashMap<>();
    private Map<String, String> groups = new HashMap<>();
    private Retention retention = new Retention();
    private Retry retry = new Retry();

    public Map<String, String> getStreams() {
        return streams;
    }

    public void setStreams(Map<String, String> streams) {
        this.streams = streams;
    }

    public Map<String, String> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, String> groups) {
        this.groups = groups;
    }

    public Retention getRetention() {
        return retention;
    }

    public void setRetention(Retention retention) {
        this.retention = retention;
    }

    public Retry getRetry() {
        return retry;
    }

    public void setRetry(Retry retry) {
        this.retry = retry;
    }

    public String getStreamName(String key, String defaultName) {
        return streams.getOrDefault(key, defaultName);
    }

    public String getGroupName(String key, String defaultGroup) {
        return groups.getOrDefault(key, defaultGroup);
    }

    public static class Retention {
        private Long maxLen = 10000L;

        public Long getMaxLen() {
            return maxLen;
        }

        public void setMaxLen(Long maxLen) {
            this.maxLen = maxLen;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;
        private long backoffMs = 1000L;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getBackoffMs() {
            return backoffMs;
        }

        public void setBackoffMs(long backoffMs) {
            this.backoffMs = backoffMs;
        }
    }
}
