package com.catchandgo.auth.supabase;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ExternalIdentityStore {
    private final JdbcTemplate jdbc;
    public ExternalIdentityStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public String ensureRole(String externalId, String role) {
        jdbc.update("INSERT INTO auth_external_identities (external_id, role) VALUES (?, ?) ON CONFLICT (external_id) DO NOTHING", externalId, role);
        return jdbc.queryForObject("SELECT role FROM auth_external_identities WHERE external_id = ?", String.class, externalId);
    }
}
