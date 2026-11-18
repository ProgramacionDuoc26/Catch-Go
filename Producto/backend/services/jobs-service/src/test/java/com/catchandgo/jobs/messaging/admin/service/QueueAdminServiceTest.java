package com.catchandgo.jobs.messaging.admin.service;

import com.catchandgo.common.events.QueueSummaryDto;
import com.catchandgo.jobs.messaging.admin.dto.CreateQueueRequest;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.catchandgo.jobs.messaging.config.StreamRetentionService;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamInfo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

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

    @Mock
    private StreamRetentionService retention;

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
        verify(streamOperations).createGroup(eq("stream:test-queue"), any(ReadOffset.class), eq("test-group"));
        verify(retention).setMaxLen("stream:test-queue", 5000L);
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
    @Test
    void createGroupDoesNotHideRedisErrors() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(true);
        when(streamOperations.createGroup(eq("stream:test-queue"), any(ReadOffset.class), eq("group")))
            .thenThrow(new RuntimeException("Redis unavailable"));
        assertThrows(RuntimeException.class, () -> service.createGroup("test-queue", "group"));
    }

    @Test
    void groupsExposeLagAndPendingSeparately() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(true);
        var groups = mock(StreamInfo.XInfoGroups.class);
        var group = mock(StreamInfo.XInfoGroup.class);
        var consumers = mock(StreamInfo.XInfoConsumers.class);
        var consumer = mock(StreamInfo.XInfoConsumer.class);
        when(streamOperations.groups("stream:test-queue")).thenReturn(groups);
        when(groups.iterator()).thenReturn(List.of(group).iterator());
        when(group.groupName()).thenReturn("workers");
        when(group.consumerCount()).thenReturn(1L);
        when(group.pendingCount()).thenReturn(2L);
        when(group.lastDeliveredId()).thenReturn("5-0");
        when(group.getRaw()).thenReturn(Map.of("lag", 7L));
        when(streamOperations.consumers("stream:test-queue", "workers")).thenReturn(consumers);
        when(consumers.iterator()).thenReturn(List.of(consumer).iterator());
        when(consumer.consumerName()).thenReturn("worker-1");
        when(consumer.pendingCount()).thenReturn(2L);
        var result = service.getGroups("test-queue").get(0);
        assertEquals(7L, result.lag());
        assertEquals(2L, result.pendingMessages());
        assertEquals("worker-1", result.consumers().get(0).name());
    }

    @Test
    void settingRetentionPersistsPolicyForExistingStream() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(true);
        service.setRetention("test-queue", 250L);
        verify(retention).setMaxLen("stream:test-queue", 250L);
    }

    @Test void deletingGroupReturnsResultFromAtomicCheck() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(true);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("stream:test-queue")), eq("group")))
                .thenReturn(-1L, -2L, -3L, 1L, 0L);
        assertEquals(QueueAdminService.GroupDeletion.PENDING, service.deleteGroup("test-queue", "group"));
        assertEquals(QueueAdminService.GroupDeletion.UNREAD, service.deleteGroup("test-queue", "group"));
        assertEquals(QueueAdminService.GroupDeletion.CONSUMERS, service.deleteGroup("test-queue", "group"));
        assertEquals(QueueAdminService.GroupDeletion.DELETED, service.deleteGroup("test-queue", "group"));
        assertEquals(QueueAdminService.GroupDeletion.NOT_FOUND, service.deleteGroup("test-queue", "group"));
    }

    @Test void cannotDeleteProtectedGroupOrUseInvalidName() {
        when(redisTemplate.hasKey("stream:job-applications")).thenReturn(true);
        when(messagingProperties.getStreamName("job-applications", "stream:job-applications")).thenReturn("stream:job-applications");
        when(messagingProperties.getGroupName("notifications-group", "notifications-group")).thenReturn("notifications-group");
        assertEquals(QueueAdminService.GroupDeletion.PROTECTED, service.deleteGroup("job-applications", "notifications-group"));
        assertThrows(IllegalArgumentException.class, () -> service.deleteGroup("job-applications", "bad/group"));
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
    }

    @Test void deletingGroupFailsWhenRedisDoesNotConfirm() {
        when(redisTemplate.hasKey("stream:test-queue")).thenReturn(true);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of("stream:test-queue")), eq("group"))).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.deleteGroup("test-queue", "group"));
    }
}
