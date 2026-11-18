package com.catchandgo.auth.supabase;

import com.catchandgo.common.jwt.JwtService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupabaseExchangeServiceTest {
    @Test void existingServerRoleOverridesNewSelection() {
        var client = mock(SupabaseIdentityClient.class);
        var store = mock(ExternalIdentityStore.class);
        var jwt = mock(JwtService.class);
        var service = new SupabaseExchangeService(client, store, jwt);
        when(client.verify("external-token")).thenReturn(new SupabaseIdentityClient.Identity("id", "u@example.com", "User", ""));
        when(store.ensureRole("id", "EMPRESA")).thenReturn("TRABAJADOR");
        when(jwt.generateToken("id", "TRABAJADOR", 86400)).thenReturn("internal-token");
        var result = service.exchange("external-token", "EMPRESA");
        assertEquals("internal-token", result.token());
        assertEquals("id", result.usuario().id());
        assertEquals("TRABAJADOR", result.usuario().tipo());
    }
    @Test void rejectsAdminBeforeCreatingAnIdentity() {
        var client = mock(SupabaseIdentityClient.class);
        var store = mock(ExternalIdentityStore.class);
        var service = new SupabaseExchangeService(client, store, mock(JwtService.class));
        assertThrows(IllegalArgumentException.class, () -> service.exchange("token", "ADMIN"));
        verifyNoInteractions(client, store);
    }
    @Test void failedVerificationDoesNotIssueToken() {
        var client = mock(SupabaseIdentityClient.class);
        var store = mock(ExternalIdentityStore.class);
        var jwt = mock(JwtService.class);
        var service = new SupabaseExchangeService(client, store, jwt);
        when(client.verify("bad")).thenThrow(new SupabaseAuthException("invalid", false));
        assertThrows(SupabaseAuthException.class, () -> service.exchange("bad", "EMPRESA"));
        verifyNoInteractions(store, jwt);
    }
}
