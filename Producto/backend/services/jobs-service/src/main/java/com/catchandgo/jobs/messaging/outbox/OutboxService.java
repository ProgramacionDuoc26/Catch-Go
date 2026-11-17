package com.catchandgo.jobs.messaging.outbox;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {
    public static final String APPLICATION_SUBMITTED = "JOB_APPLICATION_SUBMITTED";
    public static final String STATUS_CHANGED = "APPLICATION_STATUS_CHANGED";
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;

    public OutboxService(JdbcTemplate jdbc, ObjectMapper mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(JobApplicationEvent event) {
        insert(event.eventId(), APPLICATION_SUBMITTED, event);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void enqueue(ApplicationStatusEvent event) {
        insert(event.eventId(), STATUS_CHANGED, event);
    }

    private void insert(String id, String type, Object event) {
        try {
            jdbc.update("INSERT INTO jobs_event_outbox (event_id, event_type, payload) VALUES (?, ?, ?)",
                    id, type, mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar el evento", e);
        }
    }
}
