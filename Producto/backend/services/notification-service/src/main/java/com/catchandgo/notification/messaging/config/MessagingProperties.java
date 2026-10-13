package com.catchandgo.notification.messaging.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Propiedades centralizadas de mensajería para notification-service.
 */
@Configuration
@ConfigurationProperties(prefix = "app.messaging")
public class MessagingProperties {

    private Map<String, String> streams = new HashMap<>();
    private Map<String, String> groups = new HashMap<>();
    private Consumer consumer = new Consumer();
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

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
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

    public static class Consumer {
        private String name = "notification-consumer-1";
        private long pollTimeoutMs = 2000L;
        private int batchSize = 10;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPollTimeoutMs() {
            return pollTimeoutMs;
        }

        public void setPollTimeoutMs(long pollTimeoutMs) {
            this.pollTimeoutMs = pollTimeoutMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
    }

    public static class Retry {
        private int maxAttempts = 3;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }
    }
}
