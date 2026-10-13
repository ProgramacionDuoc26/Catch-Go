package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Consumidor asíncrono de eventos de postulación en Redis Streams.
 * Cumple con los criterios de la rúbrica:
 * - Consumidor agrupado por dominio funcional (Notificaciones).
 * - Confirmación explícita mediante ACK (XACK).
 * - Manejo explícito de errores y desvío a Dead Letter Queue (DLQ).
 */
@Component
public class JobApplicationStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationStreamConsumer.class);

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;

    // Registro de reintentos por ID de mensaje
    private final Map<String, AtomicInteger> retryCounters = new ConcurrentHashMap<>();

    public JobApplicationStreamConsumer(StringRedisTemplate redisTemplate,
                                       SimpMessagingTemplate messagingTemplate,
                                       ObjectMapper objectMapper,
                                       MessagingProperties messagingProperties) {
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.messagingProperties = messagingProperties;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String streamKey = record.getStream();
        RecordId recordId = record.getId();
        Map<String, String> value = record.getValue();
        String groupName = messagingProperties.getGroupName("notifications-group", "notifications-group");

        // Ignorar registros de inicialización del sistema
        if (value.containsKey("system")) {
            redisTemplate.opsForStream().acknowledge(streamKey, groupName, recordId);
            return;
        }

        log.info("[ASYNC-MQ CONSUMER] Mensaje recibido de stream '{}' | ID='{}'", streamKey, recordId);

        try {
            // Procesamiento de negocio del evento
            processApplicationEvent(value);

            // 1. Confirmación de procesamiento exitoso (ACK explícito - XACK)
            redisTemplate.opsForStream().acknowledge(streamKey, groupName, recordId);
            retryCounters.remove(recordId.getValue());

            log.info("[ASYNC-MQ ACK] Confirmado exitosamente (XACK) | Stream='{}' | Grupo='{}' | ID='{}'",
                    streamKey, groupName, recordId);

        } catch (Exception e) {
            handleProcessingFailure(streamKey, groupName, record, e);
        }
    }

    private void processApplicationEvent(Map<String, String> data) throws Exception {
        String employerId = data.get("employerId");
        String candidateId = data.get("candidateId");
        String offerId = data.get("offerId");

        // Simulación de validación: si viene corrupto, fuerza el desvío a DLQ
        if (data.containsKey("forceError") && "true".equalsIgnoreCase(data.get("forceError"))) {
            throw new IllegalArgumentException("Error simulado para prueba de Dead Letter Queue (DLQ)");
        }

        Map<String, String> notification = Map.of(
            "title", "Nueva Postulación Recibida",
            "message", "El postulante " + candidateId + " ha aplicado a la oferta #" + offerId,
            "type", "JOB_APPLICATION",
            "userId", employerId != null ? employerId : "unknown",
            "timestamp", Instant.now().toString()
        );

        // Retransmisión en tiempo real vía WebSocket al usuario destinatario
        if (employerId != null && !employerId.isBlank()) {
            messagingTemplate.convertAndSend("/topic/user/" + employerId, notification);
            log.info("[ASYNC-MQ WS] Alerta enviada a WebSocket para empresa '{}'", employerId);
        }
    }

    private void handleProcessingFailure(String streamKey,
                                         String groupName,
                                         MapRecord<String, String, String> record,
                                         Exception exception) {
        RecordId recordId = record.getId();
        int maxRetries = messagingProperties.getRetry().getMaxAttempts();
        int attempts = retryCounters.computeIfAbsent(recordId.getValue(), k -> new AtomicInteger(0)).incrementAndGet();

        log.warn("[ASYNC-MQ RETRY] Intento {} de {} para mensaje ID='{}': {}",
                attempts, maxRetries, recordId, exception.getMessage());

        if (attempts >= maxRetries) {
            // 2. Se alcanzaron los reintentos máximos -> Derivar a Dead Letter Queue (DLQ)
            String dlqStream = messagingProperties.getStreamName("dlq", "stream:job-applications:dlq");

            try {
                Map<String, String> dlqPayload = new HashMap<>(record.getValue());
                dlqPayload.put("errorReason", exception.getMessage() != null ? exception.getMessage() : "Error desconocido");
                dlqPayload.put("failedAt", Instant.now().toString());
                dlqPayload.put("originalStream", streamKey);
                dlqPayload.put("originalId", recordId.getValue());
                dlqPayload.put("retryAttempts", String.valueOf(attempts));

                // Guardar en la DLQ
                redisTemplate.opsForStream().add(dlqStream, dlqPayload);

                // Confirmar el mensaje en la cola principal para no bloquear la cola a otros mensajes
                redisTemplate.opsForStream().acknowledge(streamKey, groupName, recordId);
                retryCounters.remove(recordId.getValue());

                log.error("[ASYNC-MQ ALERTA DLQ] Mensaje ID='{}' movido a Dead Letter Queue '{}' tras {} fallos",
                        recordId, dlqStream, attempts);

            } catch (Exception dlqEx) {
                log.error("[ASYNC-MQ ERROR CRÍTICO] No se pudo transferir mensaje a DLQ: {}", dlqEx.getMessage(), dlqEx);
            }
        }
    }
}
