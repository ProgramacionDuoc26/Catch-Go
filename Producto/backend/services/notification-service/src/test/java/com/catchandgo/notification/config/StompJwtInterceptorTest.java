package com.catchandgo.notification.config;

import com.catchandgo.common.jwt.JwtService;
import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import static org.junit.jupiter.api.Assertions.*;

class StompJwtInterceptorTest {
    private static final String SECRET = "test-secret-key-with-at-least-32-bytes-for-tests";
    private final JwtService jwt = new JwtService(SECRET);
    private final StompJwtInterceptor interceptor = new StompJwtInterceptor(SECRET);

    private Message<byte[]> message(StompHeaderAccessor headers) {
        headers.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], headers.getMessageHeaders());
    }

    private Principal connect(String token) {
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (token != null) headers.setNativeHeader("Authorization", "Bearer " + token);
        interceptor.preSend(message(headers), null);
        return headers.getUser();
    }

    private void subscribe(Principal principal, String destination) {
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headers.setUser(principal);
        headers.setDestination(destination);
        interceptor.preSend(message(headers), null);
    }

    @Test
    void validUserCanSubscribeOnlyToOwnNotifications() {
        Principal principal = connect(jwt.generateToken("123", 60));
        assertEquals("123", principal.getName());
        assertDoesNotThrow(() -> subscribe(principal, "/topic/user/123"));
        assertThrows(AccessDeniedException.class, () -> subscribe(principal, "/topic/user/124"));
        assertThrows(AccessDeniedException.class, () -> subscribe(principal, "/topic/user/*"));
        assertThrows(AccessDeniedException.class, () -> subscribe(null, "/topic/user/123"));
    }

    @Test
    void rejectsMissingExpiredAndIncorrectlySignedTokens() {
        assertThrows(BadCredentialsException.class, () -> connect(null));
        assertThrows(BadCredentialsException.class, () -> connect(jwt.generateToken("123", -60)));
        JwtService other = new JwtService("another-secret-key-with-at-least-32-bytes-long");
        assertThrows(BadCredentialsException.class, () -> connect(other.generateToken("123", 60)));
    }

    @Test
    void rejectsClientMessagesToBroker() {
        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination("/topic/user/123");
        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message(headers), null));
    }
}
