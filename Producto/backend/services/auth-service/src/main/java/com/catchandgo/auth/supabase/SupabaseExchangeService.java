package com.catchandgo.auth.supabase;

import com.catchandgo.common.jwt.JwtService;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class SupabaseExchangeService {
    public record User(String id, String email, String nombre, String tipo, String foto) {}
    public record Result(String token, User usuario) {}
    private final SupabaseIdentityClient client;
    private final ExternalIdentityStore store;
    private final JwtService jwt;

    public SupabaseExchangeService(SupabaseIdentityClient client, ExternalIdentityStore store, JwtService jwt) {
        this.client = client;
        this.store = store;
        this.jwt = jwt;
    }

    public Result exchange(String accessToken, String requestedRole) {
        String role = requestedRole == null ? "" : requestedRole.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("TRABAJADOR", "EMPRESA").contains(role)) throw new IllegalArgumentException("Tipo de cuenta invalido");
        var identity = client.verify(accessToken);
        String savedRole = store.ensureRole(identity.id(), role);
        if (!Set.of("TRABAJADOR", "EMPRESA").contains(savedRole)) throw new IllegalStateException("Tipo de cuenta guardado invalido");
        return new Result(jwt.generateToken(identity.id(), savedRole, 86400),
                new User(identity.id(), identity.email(), identity.name(), savedRole, identity.photo()));
    }
}
