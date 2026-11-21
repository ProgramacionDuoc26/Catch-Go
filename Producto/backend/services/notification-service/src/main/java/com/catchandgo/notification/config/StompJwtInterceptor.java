package com.catchandgo.notification.config;

import com.catchandgo.common.jwt.JwtService;
import io.jsonwebtoken.Claims;
import java.security.Principal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

@Component
public class StompJwtInterceptor implements ChannelInterceptor {
    private final JwtService jwt;

    public StompJwtInterceptor(@Value("${JWT_SECRET:${app.jwt.secret:CatchAndGoSecretKeyForDevelopmentAndTestingPurposesOnly32Bytes}}") String secret) {
        this.jwt = new JwtService(secret);
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headers = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (headers == null || headers.getCommand() == null) return message;

        StompCommand command = headers.getCommand();
        if (command == StompCommand.CONNECT || command == StompCommand.STOMP) {
            String authorization = headers.getFirstNativeHeader("Authorization");
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                throw new BadCredentialsException("Se requiere un token de acceso");
            }
            try {
                Claims claims = jwt.parseClaims(authorization.substring(7).trim());
                String subject = claims.getSubject();
                if (subject == null || !subject.matches("[A-Za-z0-9-]+") || claims.getExpiration() == null) {
                    throw new BadCredentialsException("Token de acceso inválido");
                }
                headers.setUser(new SessionPrincipal(subject, claims.getExpiration().toInstant()));
            } catch (RuntimeException ex) {
                throw new BadCredentialsException("Token de acceso inválido", ex);
            }
        } else if (command == StompCommand.SUBSCRIBE) {
            if (!(headers.getUser() instanceof SessionPrincipal principal)
                    || !principal.expiresAt().isAfter(Instant.now())
                    || !("/topic/user/" + principal.getName()).equals(headers.getDestination())) {
                throw new AccessDeniedException("Suscripción no autorizada");
            }
        } else if (command == StompCommand.SEND) {
            throw new AccessDeniedException("Los clientes no pueden enviar notificaciones");
        }
        return message;
    }

    private record SessionPrincipal(String name, Instant expiresAt) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
