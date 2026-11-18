package com.catchandgo.jobs.messaging.producer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.catchandgo.jobs.messaging.config.StreamRetentionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StreamRetentionPublisherTest {
    StringRedisTemplate redis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked") StreamOperations<String, String, String> streams = mock(StreamOperations.class);
    StreamRetentionService retention = mock(StreamRetentionService.class);
    MessagingProperties properties = new MessagingProperties();
    ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach void setup() {
        when(redis.<String, String>opsForStream()).thenReturn(streams);
        when(streams.add(any(MapRecord.class))).thenReturn(RecordId.of("1-0"));
    }

    @Test void applicationPublisherUsesConfiguredPolicy() {
        when(retention.effectiveMaxLen("stream:job-applications")).thenReturn(250L);
        var publisher = new JobApplicationStreamPublisher(redis, mapper, properties, retention);
        publisher.publish(JobApplicationEvent.create(1L, 2L, "Offer", "worker", "Worker", "", "company"));
        verify(retention).trimSafely("stream:job-applications", 250L);
    }

    @Test void statusPublisherUsesConfiguredPolicy() {
        when(retention.effectiveMaxLen("stream:application-status")).thenReturn(400L);
        var publisher = new ApplicationStatusStreamPublisher(redis, mapper, properties, retention);
        publisher.publish(ApplicationStatusEvent.create(1L, 2L, "worker", "PENDIENTE", "ACEPTADO", "Title", "Message", "info"));
        verify(retention).trimSafely("stream:application-status", 400L);
    }

    @Test void trimFailureDoesNotRetryAnAlreadyPublishedEvent() {
        when(retention.effectiveMaxLen("stream:job-applications")).thenReturn(250L);
        when(retention.trimSafely("stream:job-applications", 250L)).thenThrow(new IllegalStateException("Redis error"));
        var publisher = new JobApplicationStreamPublisher(redis, mapper, properties, retention);
        var id = publisher.publish(JobApplicationEvent.create(1L, 2L, "Offer", "worker", "Worker", "", "company"));
        assertEquals(RecordId.of("1-0"), id);
        verify(streams, times(1)).add(any(MapRecord.class));
    }
}
