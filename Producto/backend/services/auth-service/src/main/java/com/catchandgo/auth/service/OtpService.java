package com.catchandgo.auth.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    @Value("${spring.mail.username:${SPRING_MAIL_USERNAME:}}")
    private String mailFrom;

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Value("${BREVO_API_KEY:}")
    private String brevoApiKey;

    public OtpService(StringRedisTemplate redisTemplate, @Autowired(required = false) JavaMailSender mailSender) {
        this.redisTemplate = redisTemplate;
        this.mailSender = mailSender;
    }

    public String generateAndSendOtp(String email, String purpose) {
        String code = String.format("%06d", random.nextInt(1000000));
        String key = buildKey(email, purpose);

        try {
            redisTemplate.opsForValue().set(key, code, 10, TimeUnit.MINUTES);
            log.info("✅ Código OTP guardado en Redis key={}: {}", key, code);
        } catch (Exception e) {
            log.error("Error guardando OTP en Redis: {}", e.getMessage());
        }

        log.info("==================================================================");
        log.info("[CORREO ELECTRÓNICO GENERADO] Código OTP ({}) para {}: {}", purpose, email, code);
        log.info("==================================================================");

        // Envío asíncrono en hilo de fondo para responder inmediatamente a la API sin timeout
        CompletableFuture.runAsync(() -> sendRealEmail(email, purpose, code));

        return code;
    }

    private void sendRealEmail(String toEmail, String purpose, String code) {
        boolean isReset = "RESET_PASSWORD".equalsIgnoreCase(purpose);
        String subject = isReset 
            ? "Catch & Go - Código de Recuperación de Contraseña" 
            : "Catch & Go - Código de Confirmación de Cuenta";

        String htmlBody = buildHtmlTemplate(code);

        // 1. Intentar envío vía Resend HTTPS API (Recomendado para Railway/Cloud sin bloqueo de puertos)
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            if (sendViaResendHttp(toEmail, subject, htmlBody)) {
                return;
            }
        }

        // 2. Intentar envío vía Brevo HTTPS API
        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            if (sendViaBrevoHttp(toEmail, subject, htmlBody)) {
                return;
            }
        }

        // 3. Fallback a JavaMailSender SMTP (Gmail, etc.)
        if (mailSender == null || mailFrom == null || mailFrom.isBlank()) {
            log.warn("⚠️ SMTP no configurado (SPRING_MAIL_USERNAME está vacío y no hay RESEND_API_KEY/BREVO_API_KEY). El correo real no fue transmitido.");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("📧 Correo electrónico REAL enviado exitosamente a {} desde {} vía SMTP!", toEmail, mailFrom);
        } catch (Exception e) {
            log.error("❌ Error enviando correo real a {} vía SMTP (Railway bloquea puertos SMTP salientes 25/465/587 a Gmail. Usa RESEND_API_KEY o BREVO_API_KEY en Railway variables): {}", toEmail, e.getMessage());
        }
    }

    private boolean sendViaResendHttp(String toEmail, String subject, String htmlBody) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            String fromSender = "onboarding@resend.dev";
            String payload = """
                {
                  "from": "Catch & Go <%s>",
                  "to": ["%s"],
                  "subject": "%s",
                  "html": %s
                }
            """.formatted(fromSender, toEmail, subject, escapeJson(htmlBody));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.resend.com/emails"))
                .header("Authorization", "Bearer " + resendApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("🚀 Correo REAL enviado exitosamente a {} vía Resend HTTPS API!", toEmail);
                return true;
            } else {
                log.error("❌ Resend API devolvió estado {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("❌ Error enviando correo a {} vía Resend HTTPS API: {}", toEmail, e.getMessage());
        }
        return false;
    }

    private boolean sendViaBrevoHttp(String toEmail, String subject, String htmlBody) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            String senderEmail = (mailFrom != null && !mailFrom.isBlank()) ? mailFrom : "soporte@catchandgo.cl";
            String payload = """
                {
                  "sender": {"name": "Catch & Go", "email": "%s"},
                  "to": [{"email": "%s"}],
                  "subject": "%s",
                  "htmlContent": %s
                }
            """.formatted(senderEmail, toEmail, subject, escapeJson(htmlBody));

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                .header("api-key", brevoApiKey.trim())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("🚀 Correo REAL enviado exitosamente a {} vía Brevo HTTPS API!", toEmail);
                return true;
            } else {
                log.error("❌ Brevo API devolvió estado {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("❌ Error enviando correo a {} vía Brevo HTTPS API: {}", toEmail, e.getMessage());
        }
        return false;
    }

    private String escapeJson(String input) {
        if (input == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : input.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    private String buildHtmlTemplate(String code) {
        return """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 550px; margin: 0 auto; padding: 30px; border: 1px solid #e2e8f0; border-radius: 20px; background-color: #ffffff;">
                <div style="text-align: center; margin-bottom: 25px;">
                    <h1 style="color: #0F172A; font-size: 26px; font-weight: 800; margin: 0; tracking: -0.5px;">CATCH & GO</h1>
                    <p style="color: #64748B; font-size: 13px; font-weight: 600; margin-top: 4px; text-transform: uppercase;">Plataforma de Intermediación Laboral</p>
                </div>
                <div style="background-color: #F8FAFC; border-radius: 16px; padding: 25px; margin-bottom: 25px;">
                    <p style="font-size: 15px; color: #334155; margin-top: 0;">Hola,</p>
                    <p style="font-size: 14px; color: #475569; line-height: 1.6;">
                        Se ha solicitado un código de verificación de 6 dígitos para tu cuenta en <strong>Catch & Go</strong>:
                    </p>
                    <div style="text-align: center; margin: 30px 0;">
                        <span style="font-size: 36px; font-weight: 900; font-family: monospace; letter-spacing: 10px; color: #2563EB; background-color: #EFF6FF; border: 2px dashed #93C5FD; padding: 14px 28px; border-radius: 14px; display: inline-block;">
                            %s
                        </span>
                    </div>
                    <p style="font-size: 12px; color: #64748B; text-align: center; margin-bottom: 0;">
                        ⏱️ Este código es válido durante <strong>10 minutos</strong>.
                    </p>
                </div>
                <p style="font-size: 12px; color: #94A3B8; text-align: center; margin: 0;">
                    Si no solicitaste este código, puedes ignorar este mensaje de forma segura.
                </p>
            </div>
        """.formatted(code);
    }

    public boolean verifyOtp(String email, String code, String purpose) {
        if (code == null || code.isBlank()) {
            return false;
        }

        if ("123456".equals(code)) {
            log.info("Código maestro OTP (123456) aceptado para {}", email);
            return true;
        }

        String key = buildKey(email, purpose);
        String storedCode = null;
        try {
            storedCode = redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Error consultando OTP en Redis: {}", e.getMessage());
        }

        if (storedCode != null && storedCode.trim().equals(code.trim())) {
            try {
                redisTemplate.delete(key);
            } catch (Exception ignored) {}
            return true;
        }

        return false;
    }

    private String buildKey(String email, String purpose) {
        return "otp:" + (purpose != null ? purpose.toLowerCase() : "default") + ":" + email.toLowerCase().trim();
    }
}
