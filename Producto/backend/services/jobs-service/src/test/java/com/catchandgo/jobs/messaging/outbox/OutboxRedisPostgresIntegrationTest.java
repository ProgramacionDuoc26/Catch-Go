package com.catchandgo.jobs.messaging.outbox;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.jobs.entity.JobOffer;
import com.catchandgo.jobs.messaging.producer.JobApplicationStreamPublisher;
import com.catchandgo.jobs.repository.JobOfferRepository;
import com.catchandgo.jobs.service.JobOfferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
    "JWT_SECRET=integration-test-secret-at-least-32-bytes-long",
    "app.messaging.outbox.initial-delay-ms=3600000",
    "app.messaging.outbox.poll-delay-ms=3600000"
})
class OutboxRedisPostgresIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    @Container static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired JobOfferRepository offers;
    @Autowired JobOfferService jobs;
    @Autowired JdbcTemplate jdbc;
    @Autowired StringRedisTemplate redisTemplate;
    @Autowired ObjectMapper mapper;
    @Autowired OutboxRelay relay;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
    @SpyBean JobApplicationStreamPublisher appPublisher;

    @BeforeEach void resetData() {
        jdbc.update("DELETE FROM jobs_event_outbox");
        jdbc.update("DELETE FROM job_applications");
        jdbc.update("DELETE FROM job_offers");
        redisTemplate.opsForStream().trim("stream:job-applications", 0);
        redisTemplate.opsForStream().trim("stream:application-status", 0);
    }

    private long createOffer() {
        JobOffer offer = new JobOffer();
        offer.setTitulo("Turno de prueba");
        offer.setEmpresaId("empresa-1");
        offer.setEstado("ABIERTA");
        return offers.save(offer).getId();
    }

    @Test void bothBusinessEventsArePublishedFromCommittedOutbox() throws Exception {
        long offerId = createOffer();
        jobs.apply(offerId, "trabajador-1");
        Long applicationId = jdbc.queryForObject("SELECT id FROM job_applications", Long.class);
        jobs.updateApplicationStatus(applicationId, "ACEPTADO");
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM jobs_event_outbox WHERE published_at IS NULL", Integer.class));

        relay.relayPending();
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM jobs_event_outbox WHERE published_at IS NULL", Integer.class));
        var applications = redisTemplate.<String, String>opsForStream().range("stream:job-applications", Range.unbounded());
        var statuses = redisTemplate.<String, String>opsForStream().range("stream:application-status", Range.unbounded());
        assertNotNull(applications);
        assertNotNull(statuses);
        assertEquals(1, applications.size());
        assertEquals(1, statuses.size());
        var application = mapper.readValue(applications.get(0).getValue().get("payload"), JobApplicationEvent.class);
        var status = mapper.readValue(statuses.get(0).getValue().get("payload"), ApplicationStatusEvent.class);
        assertEquals(applicationId, application.applicationId());
        assertEquals("empresa-1", application.employerId());
        assertEquals("ACEPTADO", status.newStatus());
        assertEquals("trabajador-1", status.workerId());
    }

    @Test void failedTransactionRollsBackApplicationAndOutboxTogether() {
        long offerId = createOffer();
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        assertThrows(IllegalStateException.class, () -> transaction.executeWithoutResult(status -> {
            jobs.apply(offerId, "trabajador-rollback");
            throw new IllegalStateException("forzar rollback");
        }));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM job_applications", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM jobs_event_outbox", Integer.class));
    }

    @Test void failedPublicationRemainsPendingAndCanBeRetried() {
        long offerId = createOffer();
        jobs.apply(offerId, "trabajador-2");
        doThrow(new IllegalStateException("fallo simulado"))
                .doCallRealMethod().when(appPublisher).publish(any(JobApplicationEvent.class));
        relay.relayPending();
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM jobs_event_outbox WHERE published_at IS NULL AND attempts = 1", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM job_applications", Integer.class));
        relay.relayPending();
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM jobs_event_outbox WHERE published_at IS NULL", Integer.class));
        assertEquals(1L, redisTemplate.opsForStream().size("stream:job-applications"));
    }
}
