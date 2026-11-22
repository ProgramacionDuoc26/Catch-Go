package com.catchandgo.auth.supabase;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ExternalIdentityStore {
    private final JdbcTemplate jdbc;
    public ExternalIdentityStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @PostConstruct
    public void init() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS auth_external_identities (" +
                    "external_id VARCHAR(36) PRIMARY KEY, " +
                    "role VARCHAR(20) NOT NULL CHECK (role IN ('TRABAJADOR', 'EMPRESA')), " +
                    "created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        } catch (Exception ignored) {
        }
    }

    @Transactional
    public String ensureRole(String externalId, String role) {
        jdbc.update("INSERT INTO auth_external_identities (external_id, role) VALUES (?, ?) ON CONFLICT (external_id) DO NOTHING", externalId, role);
        return jdbc.queryForObject("SELECT role FROM auth_external_identities WHERE external_id = ?", String.class, externalId);
    }
}

