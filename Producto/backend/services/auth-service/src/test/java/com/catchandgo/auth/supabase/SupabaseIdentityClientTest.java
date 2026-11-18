package com.catchandgo.auth.supabase;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class SupabaseIdentityClientTest {
    @Test void verifiesTokenAgainstSupabaseAndUsesReturnedIdentity() {
        var builder = RestClient.builder();
        var client = new SupabaseIdentityClient(builder, "https://example.supabase.co", "public-key");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(once(), requestTo("https://example.supabase.co/auth/v1/user"))
                .andExpect(method(HttpMethod.GET)).andExpect(header("apikey", "public-key"))
                .andExpect(header("Authorization", "Bearer user-token"))
                .andRespond(withSuccess("{\"id\":\"11111111-1111-1111-1111-111111111111\",\"email\":\"user@example.com\",\"app_metadata\":{\"provider\":\"google\"},\"user_metadata\":{\"full_name\":\"Test User\"}}", MediaType.APPLICATION_JSON));
        var identity = client.verify("user-token");
        assertEquals("11111111-1111-1111-1111-111111111111", identity.id());
        assertEquals("Test User", identity.name());
        server.verify();
    }
    @Test void rejectsInvalidToken() {
        var builder = RestClient.builder();
        var client = new SupabaseIdentityClient(builder, "https://example.supabase.co", "public-key");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://example.supabase.co/auth/v1/user")).andRespond(withUnauthorizedRequest());
        var ex = assertThrows(SupabaseAuthException.class, () -> client.verify("bad-token"));
        assertFalse(ex.isUnavailable());
        server.verify();
    }
    @Test void rejectsUserWithoutServerConfirmedGoogleProvider() {
        var builder = RestClient.builder();
        var client = new SupabaseIdentityClient(builder, "https://example.supabase.co", "public-key");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://example.supabase.co/auth/v1/user"))
                .andRespond(withSuccess("{\"id\":\"11111111-1111-1111-1111-111111111111\",\"app_metadata\":{\"provider\":\"email\"},\"user_metadata\":{\"provider\":\"google\"}}", MediaType.APPLICATION_JSON));
        assertThrows(SupabaseAuthException.class, () -> client.verify("user-token"));
        server.verify();
    }
}
