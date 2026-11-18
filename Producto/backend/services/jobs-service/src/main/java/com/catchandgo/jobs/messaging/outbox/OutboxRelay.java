package com.catchandgo.jobs.messaging.outbox;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.jobs.messaging.producer.ApplicationStatusStreamPublisher;
import com.catchandgo.jobs.messaging.producer.JobApplicationStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final JobApplicationStreamPublisher applicationPublisher;
    private final ApplicationStatusStreamPublisher statusPublisher;
    private final TransactionTemplate transactions;

    record PendingEvent(String id, String type, String payload) {}

    public OutboxRelay(JdbcTemplate jdbc, ObjectMapper mapper, JobApplicationStreamPublisher applicationPublisher,
                       ApplicationStatusStreamPublisher statusPublisher, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.applicationPublisher = applicationPublisher;
        this.statusPublisher = statusPublisher;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelayString = "${app.messaging.outbox.poll-delay-ms:5000}",
            initialDelayString = "${app.messaging.outbox.initial-delay-ms:5000}")
    public void relayPending() {
        try {
            transactions.executeWithoutResult(status -> {
                List<PendingEvent> events = jdbc.query(
                    "SELECT event_id, event_type, payload FROM jobs_event_outbox " +
                    "WHERE published_at IS NULL ORDER BY attempts, created_at, event_id LIMIT 20 FOR UPDATE SKIP LOCKED",
                    (rs, rowNum) -> new PendingEvent(rs.getString(1), rs.getString(2), rs.getString(3)));
                for (PendingEvent event : events) publish(event);
            });
        } catch (Exception e) {
            log.error("No se pudieron procesar los eventos pendientes del outbox", e);
        }
    }

    private void publish(PendingEvent event) {
        try {
            switch (event.type()) {
                case OutboxService.APPLICATION_SUBMITTED -> applicationPublisher.publish(mapper.readValue(event.payload(), JobApplicationEvent.class));
                case OutboxService.STATUS_CHANGED -> statusPublisher.publish(mapper.readValue(event.payload(), ApplicationStatusEvent.class));
                default -> throw new IllegalArgumentException("Tipo de evento desconocido: " + event.type());
            }
        } catch (Exception e) {
            log.warn("No se pudo publicar evento {}; se intentara nuevamente: {}", event.id(), e.getMessage());
            jdbc.update("UPDATE jobs_event_outbox SET attempts = attempts + 1, last_error = ? WHERE event_id = ?",
                    e.getClass().getSimpleName(), event.id());
            return;
        }
        jdbc.update("UPDATE jobs_event_outbox SET published_at = CURRENT_TIMESTAMP, attempts = attempts + 1, last_error = NULL WHERE event_id = ?", event.id());
    }
}
