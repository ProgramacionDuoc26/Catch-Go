package com.catchandgo.jobs.messaging.outbox;

import com.catchandgo.common.events.JobApplicationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;

class OutboxServiceTransactionTest {
    @Test void businessChangeAndOutboxCommitOrRollbackTogether() {
        JdbcDataSource source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:outbox-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        JdbcTemplate jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE domain_change (id BIGINT PRIMARY KEY)");
        jdbc.execute("CREATE TABLE jobs_event_outbox (event_id VARCHAR(64) PRIMARY KEY, event_type VARCHAR(64), payload TEXT)");
        var service = new OutboxService(jdbc, new ObjectMapper().findAndRegisterModules());
        var tx = new TransactionTemplate(new DataSourceTransactionManager(source));
        var event = JobApplicationEvent.create(1L, 2L, "Offer", "worker", "Worker", "", "company");

        assertThrows(IllegalStateException.class, () -> tx.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO domain_change (id) VALUES (1)");
            service.enqueue(event);
            throw new IllegalStateException("force rollback");
        }));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM domain_change", Integer.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM jobs_event_outbox", Integer.class));

        tx.executeWithoutResult(status -> {
            jdbc.update("INSERT INTO domain_change (id) VALUES (1)");
            service.enqueue(event);
        });
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM domain_change", Integer.class));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM jobs_event_outbox", Integer.class));
    }
}
