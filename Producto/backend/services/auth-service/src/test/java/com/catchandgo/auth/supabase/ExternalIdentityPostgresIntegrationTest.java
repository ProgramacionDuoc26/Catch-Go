package com.catchandgo.auth.supabase;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
class ExternalIdentityPostgresIntegrationTest {
    @Container static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test void migrationCreatesRoleStoreAndExistingRoleCannotBeOverwritten() {
        var source = new DriverManagerDataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        Flyway.configure().dataSource(source).locations("classpath:db/migration").load().migrate();
        var store = new ExternalIdentityStore(new JdbcTemplate(source));
        String id = "11111111-1111-1111-1111-111111111111";
        assertEquals("TRABAJADOR", store.ensureRole(id, "TRABAJADOR"));
        assertEquals("TRABAJADOR", store.ensureRole(id, "EMPRESA"));
    }
}
