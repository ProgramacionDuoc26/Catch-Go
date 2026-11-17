package com.catchandgo.jobs.messaging.outbox;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.jobs.messaging.producer.ApplicationStatusStreamPublisher;
import com.catchandgo.jobs.messaging.producer.JobApplicationStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OutboxRelayTest {
    @Test void redisFailureKeepsEventPendingAndNextEventCanBeSent() throws Exception {
        var jdbc = mock(JdbcTemplate.class);
        var transactionManager = mock(PlatformTransactionManager.class);
        when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        var appPublisher = mock(JobApplicationStreamPublisher.class);
        var statusPublisher = mock(ApplicationStatusStreamPublisher.class);
        var mapper = new ObjectMapper().findAndRegisterModules();
        var app = JobApplicationEvent.create(1L, 2L, "Offer", "worker", "Worker", "", "company");
        var status = ApplicationStatusEvent.create(1L, 2L, "worker", "PENDIENTE", "ACEPTADO", "Title", "Message", "info");
        var pending = List.of(new OutboxRelay.PendingEvent(app.eventId(), OutboxService.APPLICATION_SUBMITTED, mapper.writeValueAsString(app)),
                new OutboxRelay.PendingEvent(status.eventId(), OutboxService.STATUS_CHANGED, mapper.writeValueAsString(status)));
        when(jdbc.query(anyString(), org.mockito.ArgumentMatchers.<RowMapper<OutboxRelay.PendingEvent>>any())).thenReturn(pending);
        when(appPublisher.publish(any())).thenThrow(new RuntimeException("Redis unavailable"));
        when(statusPublisher.publish(any())).thenReturn(RecordId.of("2-0"));
        new OutboxRelay(jdbc, mapper, appPublisher, statusPublisher, transactionManager).relayPending();
        verify(jdbc).update(contains("last_error = ?"), eq("RuntimeException"), eq(app.eventId()));
        verify(jdbc).update(contains("published_at = CURRENT_TIMESTAMP"), eq(status.eventId()));
        verify(jdbc, never()).update(contains("published_at = CURRENT_TIMESTAMP"), eq(app.eventId()));
        verify(transactionManager).commit(any());
    }
}
