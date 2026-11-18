package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.common.events.ApplicationStatusEvent;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Component
public class ApplicationStatusStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private static final Logger log = LoggerFactory.getLogger(ApplicationStatusStreamConsumer.class);
    private final StringRedisTemplate redis;
    private final SimpMessagingTemplate websocket;
    private final ObjectMapper mapper;
    private final MessagingProperties properties;
    private final EventDeliveryGuard deliveryGuard;

    public ApplicationStatusStreamConsumer(StringRedisTemplate redis, SimpMessagingTemplate websocket,
            ObjectMapper mapper, MessagingProperties properties, EventDeliveryGuard deliveryGuard) {
        this.redis = redis;
        this.websocket = websocket;
        this.mapper = mapper;
        this.properties = properties;
        this.deliveryGuard = deliveryGuard;
    }

    // Recupera mensajes abandonados por consumidores caidos despues de un minuto.
    @org.springframework.scheduling.annotation.Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void recoverPending() {
        String stream = properties.getStreamName("application-status", "stream:application-status");
        String group = properties.getGroupName("application-status-group", "application-status-notifications");
        try {
            var pending = redis.opsForStream().pending(stream, group, org.springframework.data.domain.Range.unbounded(), 100);
            if (pending == null) return;
            for (var message : pending) {
                if (message.getElapsedTimeSinceLastDelivery().compareTo(java.time.Duration.ofSeconds(60)) < 0) continue;
                var claimed = redis.<String, String>opsForStream().claim(stream, group, properties.getConsumer().getName(),
                        java.time.Duration.ofSeconds(60), message.getId());
                if (claimed != null) claimed.forEach(this::onMessage);
            }
        } catch (Exception e) {
            log.warn("No se pudieron recuperar mensajes pendientes: {}", e.getMessage());
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        String group = properties.getGroupName("application-status-group", "application-status-notifications");
        if (record.getValue().containsKey("system")) {
            redis.opsForStream().acknowledge(record.getStream(), group, record.getId());
            return;
        }
        ApplicationStatusEvent event;
        try {
            event = mapper.readValue(record.getValue().get("payload"), ApplicationStatusEvent.class);
            if (event == null || event.eventId() == null || event.eventId().isBlank() || event.workerId() == null ||
                    event.workerId().isBlank() || event.title() == null || event.message() == null ||
                    event.notificationType() == null || event.occurredAt() == null) {
                throw new IllegalArgumentException("Evento de estado incompleto");
            }
        } catch (Exception e) {
            moveToDlq(record, group, e, 1);
            return;
        }
        final EventDeliveryGuard.Claim claim;
        try {
            claim = deliveryGuard.acquire(group, event.eventId());
            if (claim.state() == EventDeliveryGuard.State.COMPLETED) {
                redis.opsForStream().acknowledge(record.getStream(), group, record.getId());
                return;
            }
            if (claim.state() == EventDeliveryGuard.State.BUSY) return;
        } catch (Exception e) {
            log.warn("No se pudo reservar el evento {}; permanece pendiente", event.eventId(), e);
            return;
        }
        try {
            websocket.convertAndSend("/topic/user/" + event.workerId(), Map.of(
                    "userId", event.workerId(), "title", event.title(), "message", event.message(),
                    "type", event.notificationType(), "timestamp", event.occurredAt().toString(), "eventId", event.eventId()));
        } catch (Exception failure) {
            try { deliveryGuard.release(claim); }
            catch (Exception e) { log.warn("No se pudo liberar la reserva del evento {}", event.eventId(), e); }
            retryOrMoveToDlq(record, group, failure);
            return;
        }
        try {
            deliveryGuard.complete(claim);
            redis.opsForStream().acknowledge(record.getStream(), group, record.getId());
        } catch (Exception e) {
            log.error("No se pudo confirmar el evento {}; permanece pendiente", event.eventId(), e);
        }
    }

    private void retryOrMoveToDlq(MapRecord<String, String, String> record, String group, Exception failure) {
        try {
            String id = record.getId().getValue();
            PendingMessages pending = redis.opsForStream().pending(record.getStream(), group, Range.closed(id, id), 1);
            if (pending == null || pending.isEmpty()) {
                log.error("No se encontro el mensaje pendiente {} en {}; no se confirmara", id, record.getStream());
                return;
            }
            long attempts = pending.get(0).getTotalDeliveryCount();
            int maximum = Math.max(1, properties.getRetry().getMaxAttempts());
            log.warn("Error procesando {} en {}: entrega {} de {}", id, record.getStream(), attempts, maximum, failure);
            if (attempts >= maximum) moveToDlq(record, group, failure, attempts);
        } catch (Exception e) {
            log.error("No se pudo consultar los intentos de {}; permanece pendiente", record.getId(), e);
        }
    }

    private void moveToDlq(MapRecord<String, String, String> record, String group, Exception failure, long attempts) {
        try {
            Map<String, String> dlq = new HashMap<>(record.getValue());
            dlq.put("originalStream", record.getStream());
            dlq.put("originalId", record.getId().getValue());
            dlq.put("failedAt", Instant.now().toString());
            dlq.put("retryAttempts", String.valueOf(attempts));
            dlq.put("errorReason", String.valueOf(failure.getMessage()));
            String dlqStream = properties.getStreamName("application-status-dlq", "stream:application-status:dlq");
            if (redis.opsForStream().add(dlqStream, dlq) == null) throw new IllegalStateException("Redis no confirmo la DLQ");
            redis.opsForStream().acknowledge(record.getStream(), group, record.getId());
        } catch (Exception e) {
            log.error("No se pudo trasladar el evento {} a la DLQ; permanece pendiente", record.getId(), e);
        }
    }
}

