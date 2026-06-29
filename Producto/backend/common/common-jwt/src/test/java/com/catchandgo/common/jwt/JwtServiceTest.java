package com.catchandgo.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;
    private final String secret = "test-secret-key-1234567890-test-secret-key-1234567890"; // Clave de prueba suficientemente larga

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret);
    }

    // CP-65: Generación exitosa de JWT
    // Verifica que generateToken devuelva un token JWT compacto no nulo y con formato esperado (3 secciones separadas por puntos)
    @Test
    void generateToken_success() {
        String token = jwtService.generateToken("user@test.com", 3600);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // Un JWT válido consta de tres partes separadas por puntos (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    // CP-66: Lectura correcta de Claims
    // Verifica que parseClaims decodifique y extraiga exitosamente el subject y la expiración correctos
    @Test
    void parseClaims_success() {
        String subject = "test-user-id-999";
        long expiresInSeconds = 1800; // 30 minutos
        String token = jwtService.generateToken(subject, expiresInSeconds);

        Claims claims = jwtService.parseClaims(token);
        assertNotNull(claims);
        assertEquals(subject, claims.getSubject());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(new java.util.Date()));
    }

    // CP-67: Detección de alteración/tampering
    // Verifica que parseClaims arroje una excepción de tipo SignatureException si el token ha sido alterado
    @Test
    void parseClaims_tamperedToken_throwsSignatureException() {
        String token = jwtService.generateToken("user@test.com", 3600);
        // Alteramos el token (por ejemplo, cambiando el último caracter de la firma)
        String tamperedToken = token.substring(0, token.length() - 1) + (token.charAt(token.length() - 1) == 'A' ? 'B' : 'A');

        assertThrows(SignatureException.class, () -> {
            jwtService.parseClaims(tamperedToken);
        });
    }

    // CP-68: Control de expiración de token
    // Verifica que parseClaims lance una ExpiredJwtException si el token ha expirado
    @Test
    void parseClaims_expiredToken_throwsExpiredJwtException() {
        // Generar un token con duración de -10 segundos (ya expirado)
        String token = jwtService.generateToken("expired@test.com", -10);

        assertThrows(ExpiredJwtException.class, () -> {
            jwtService.parseClaims(token);
        });
    }
}
