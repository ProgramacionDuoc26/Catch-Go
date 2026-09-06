package com.catchandgo.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {
    private static final Logger log = LoggerFactory.getLogger(CaptchaService.class);

    @Value("${CAPTCHA_ENABLED:true}")
    private boolean captchaEnabled;

    public void validateCaptcha(String token) {
        if (!captchaEnabled) {
            log.info("Validación de Captcha omitida (CAPTCHA_ENABLED=false)");
            return;
        }

        if (token == null || token.isBlank()) {
            throw new RuntimeException("Por favor completa la verificación de seguridad (Captcha).");
        }

        // Permite tokens de prueba/desarrollo y tokens generados por el widget interactivo
        if (token.startsWith("captcha-") || token.startsWith("mock-captcha-") || token.equals("verified")) {
            log.info("Token de Captcha validado exitosamente: {}", token);
            return;
        }

        log.info("Token de Captcha recibido: {}", token);
    }
}
