package com.catchandgo.jobs.messaging.admin.service;

import com.catchandgo.common.events.QueueSummaryDto;
import com.catchandgo.jobs.messaging.admin.dto.CreateQueueRequest;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QueueAdminServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private MessagingProperties messagingProperties;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private QueueAdminService service;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    void createQueue_success() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(false);
        when(redisTemplate.hasKey("stream:test-queue:dlq")).thenReturn(false);
        when(streamOperations.add(eq("stream:test-queue"), any(Map.class))).thenReturn(RecordId.of("1710000000000-0"));
        when(streamOperations.size("stream:test-queue")).thenReturn(1L);

        CreateQueueRequest request = new CreateQueueRequest("test-queue", "test-group", 5000L);
        QueueSummaryDto result = service.createQueue(request);

        assertNotNull(result);
        assertEquals("stream:test-queue", result.queueName());
        verify(streamOperations).createGroup("stream:test-queue", "test-group");
    }

    @Test
    void deleteQueue_success() {
        when(redisTemplate.delete("stream:test-queue")).thenReturn(true);

        boolean deleted = service.deleteQueue("test-queue");
        assertTrue(deleted);
        verify(redisTemplate).delete("stream:test-queue");
        verify(redisTemplate).delete("stream:test-queue:dlq");
    }

    @Test
    void purgeQueue_success() {
        when(streamOperations.trim(eq("stream:test-queue"), eq(0L))).thenReturn(5L);

        boolean purged = service.purgeQueue("test-queue");
        assertTrue(purged);
        verify(streamOperations).trim("stream:test-queue", 0L);
    }
}
