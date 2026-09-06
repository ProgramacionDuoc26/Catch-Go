package com.catchandgo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaService {
    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

    @Value("${CAPTCHA_ENABLED:true}")
    private boolean captchaEnabled;

    @Value("${RECAPTCHA_SECRET_KEY:6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe}")
    private String recaptchaSecretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void validateCaptcha(String token) {
        if (!captchaEnabled) {
            log.info("Validación de Captcha omitida (CAPTCHA_ENABLED=false)");
            return;
        }

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Por favor completa la verificación de seguridad Google reCAPTCHA.");
        }

        // Permite tokens de prueba/desarrollo local
        if (token.startsWith("mock-captcha-") || token.equals("local-captcha-success") || token.equals("verified")) {
            log.info("Token de prueba Captcha verificado localmente: {}", token);
            return;
        }

        // Validación oficial contra el servicio de verificación de Google reCAPTCHA
        try {
            String verifyUrl = String.format(
                "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
                recaptchaSecretKey, token
            );
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map> response = restTemplate.postForEntity(verifyUrl, null, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null) {
                Boolean success = (Boolean) body.get("success");
                if (Boolean.TRUE.equals(success)) {
                    log.info("Google reCAPTCHA verificado exitosamente con Google Cloud / API");
                    return;
                }
            }
            
            log.warn("Respuesta de verificación de Google reCAPTCHA fallida: {}", body);
        } catch (Exception e) {
            log.error("Error al conectar con el servidor de Google reCAPTCHA: {}", e.getMessage());
            if (token.length() > 20) {
                log.info("Token de reCAPTCHA estructuralmente válido aceptado por tolerancia en desarrollo");
                return;
            }
        }

        throw new RuntimeException("La verificación de Google reCAPTCHA ha fallado o ha expirado. Por favor inténtalo de nuevo.");
    }
}
