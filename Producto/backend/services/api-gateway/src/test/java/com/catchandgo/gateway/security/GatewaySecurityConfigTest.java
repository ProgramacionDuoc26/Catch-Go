package com.catchandgo.gateway.security;

import com.catchandgo.common.jwt.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET=test-secret-key-with-at-least-32-bytes-for-tests")
@AutoConfigureWebTestClient
@Import(GatewaySecurityConfigTest.TestEndpoints.class)
class GatewaySecurityConfigTest {
    @Autowired WebTestClient client;
    @Autowired JwtService jwt;

    @RestController
    static class TestEndpoints {
        @GetMapping("/protected-test") String protectedEndpoint() { return "ok"; }
        @PostMapping("/auth/login") String login() { return "ok"; }
        @PostMapping("/auth/supabase/exchange") String exchange() { return "ok"; }
        @GetMapping("/queues") String queues() { return "ok"; }
    }

    @Test void publicLoginIsAccessible() {
        client.post().uri("/auth/login").exchange().expectStatus().isOk();
    }
    @Test void publicLoginAcceptsRequestWithExpiredPreviousToken() {
        client.post().uri("/auth/login").headers(h -> h.setBearerAuth(jwt.generateToken("1", -60)))
                .exchange().expectStatus().isOk();
    }
    @Test void protectedRouteRejectsMissingToken() {
        client.get().uri("/protected-test").exchange().expectStatus().isUnauthorized();
    }
    @Test void protectedRouteRejectsInvalidSignature() {
        String token = new JwtService("different-secret-key-with-at-least-32-bytes").generateToken("1", 60);
        client.get().uri("/protected-test").headers(h -> h.setBearerAuth(token)).exchange().expectStatus().isUnauthorized();
    }
    @Test void protectedRouteRejectsExpiredToken() {
        client.get().uri("/protected-test").headers(h -> h.setBearerAuth(jwt.generateToken("1", -60)))
                .exchange().expectStatus().isUnauthorized();
    }
    @Test void protectedRouteRejectsTokenWithoutExpiry() {
        String token = Jwts.builder().subject("1").signWith(Keys.hmacShaKeyFor(
                "test-secret-key-with-at-least-32-bytes-for-tests".getBytes(StandardCharsets.UTF_8))).compact();
        client.get().uri("/protected-test").headers(h -> h.setBearerAuth(token)).exchange().expectStatus().isUnauthorized();
    }
    @Test void protectedRouteAllowsValidToken() {
        client.get().uri("/protected-test").headers(h -> h.setBearerAuth(jwt.generateToken("1", 60)))
                .exchange().expectStatus().isOk().expectBody(String.class).isEqualTo("ok");
    }
    @Test void queueAdminRejectsAnonymousAndNonAdminTokens() {
        client.get().uri("/queues").exchange().expectStatus().isUnauthorized();
        client.get().uri("/queues").headers(h -> h.setBearerAuth(jwt.generateToken("1", "TRABAJADOR", 60)))
                .exchange().expectStatus().isForbidden();
        client.get().uri("/queues").headers(h -> h.setBearerAuth(jwt.generateToken("1", 60)))
                .exchange().expectStatus().isForbidden();
    }
    @Test void queueAdminAllowsAdmin() {
        client.get().uri("/queues").headers(h -> h.setBearerAuth(jwt.generateToken("1", "ADMIN", 60)))
                .exchange().expectStatus().isOk();
    }
    @Test void supabaseExchangeIsPublicEvenWithExpiredPreviousToken() {
        client.post().uri("/auth/supabase/exchange").headers(h -> h.setBearerAuth(jwt.generateToken("1", -60)))
                .exchange().expectStatus().isOk();
    }
}
