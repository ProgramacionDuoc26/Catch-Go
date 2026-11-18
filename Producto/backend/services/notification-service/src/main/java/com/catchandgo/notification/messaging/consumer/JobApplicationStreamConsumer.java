package com.catchandgo.notification.messaging.consumer;

import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.notification.messaging.config.MessagingProperties;
import com.catchandgo.notification.hub.NotificationBroadcaster;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    private static final Logger log = LoggerFactory.getLogger(JobApplicationStreamConsumer.class);
    private static final Duration MIN_IDLE = Duration.ofSeconds(60);
    private final StringRedisTemplate redis;
    private final NotificationBroadcaster broadcaster;
    private final ObjectMapper mapper;
    private final MessagingProperties properties;
    private final EventDeliveryGuard deliveryGuard;

    public JobApplicationStreamConsumer(StringRedisTemplate redis, NotificationBroadcaster broadcaster,
                                        ObjectMapper mapper, MessagingProperties properties, EventDeliveryGuard deliveryGuard) {
        this.redis = redis;
        this.broadcaster = broadcaster;
        this.mapper = mapper;
        this.properties = properties;
        this.deliveryGuard = deliveryGuard;
    }

    private String group() {
        return properties.getGroupName("notifications-group", "notifications-group");
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        if (record.getValue().containsKey("system")) {
            acknowledge(record);
            return;
        }
        JobApplicationEvent event;
        try {
            event = mapper.readValue(record.getValue().get("payload"), JobApplicationEvent.class);
            if (event == null || event.eventId() == null || event.eventId().isBlank()
                    || event.applicationId() == null || event.applicationId() <= 0
                    || event.offerId() == null || event.offerId() <= 0
                    || event.candidateId() == null || event.candidateId().isBlank()
                    || event.employerId() == null || event.employerId().isBlank()
                    || event.timestamp() == null) {
                throw new IllegalArgumentException("Evento de postulacion incompleto");
            }
        } catch (Exception e) {
            handleFailure(record, e);
            return;
        }
        final EventDeliveryGuard.Claim claim;
        try {
            claim = deliveryGuard.acquire(group(), event.eventId());
            if (claim.state() == EventDeliveryGuard.State.COMPLETED) { acknowledge(record); return; }
            if (claim.state() == EventDeliveryGuard.State.BUSY) return;
        } catch (Exception e) {
            log.warn("No se pudo reservar el evento {}; permanece pendiente", event.eventId(), e);
            return;
        }
        try {
            Map<String, String> notification = Map.of(
                    "title", "Nueva Postulaci\u00f3n Recibida",
                    "message", "El postulante " + event.candidateId() + " ha aplicado a la oferta #" + event.offerId(),
                    "type", "info", "userId", event.employerId(), "eventId", event.eventId(), "timestamp", Instant.now().toString());
            broadcaster.publish(notification);
        } catch (Exception e) {
            try { deliveryGuard.release(claim); }
            catch (Exception releaseError) { log.warn("No se pudo liberar la reserva del evento {}", event.eventId(), releaseError); }
            handleFailure(record, e);
            return;
        }
        try {
            deliveryGuard.complete(claim);
        } catch (Exception e) {
            log.error("No se pudo guardar la confirmacion del evento {}; permanece pendiente", event.eventId(), e);
            return;
        }
        acknowledge(record);
    }

    private void acknowledge(MapRecord<String, String, String> record) {
        try {
            redis.opsForStream().acknowledge(record.getStream(), group(), record.getId());
        } catch (Exception e) {
            log.error("No se pudo confirmar el mensaje {} del stream {}; queda pendiente", record.getId(), record.getStream(), e);
        }
    }

    private void handleFailure(MapRecord<String, String, String> record, Exception failure) {
        try {
            String id = record.getId().getValue();
            PendingMessages pending = redis.opsForStream().pending(record.getStream(), group(), Range.closed(id, id), 1);
            if (pending == null || pending.isEmpty()) {
                log.error("No se encontro el mensaje pendiente {} en {}; no se confirmara", id, record.getStream());
                return;
            }
            long attempts = pending.get(0).getTotalDeliveryCount();
            log.warn("Error procesando {} en {}: entrega {} de {}", id, record.getStream(), attempts,
                    properties.getRetry().getMaxAttempts(), failure);
            if (attempts < Math.max(1, properties.getRetry().getMaxAttempts())) return;

            Map<String, String> deadLetter = new HashMap<>(record.getValue());
            deadLetter.put("errorReason", failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage());
            deadLetter.put("failedAt", Instant.now().toString());
            deadLetter.put("originalStream", record.getStream());
            deadLetter.put("originalId", id);
            deadLetter.put("retryAttempts", String.valueOf(attempts));
            String dlq = properties.getStreamName("dlq", "stream:job-applications:dlq");
            RecordId dlqId = redis.opsForStream().add(dlq, deadLetter);
            if (dlqId == null) throw new IllegalStateException("Redis no confirmo la escritura en la DLQ");
            acknowledge(record);
        } catch (Exception e) {
            log.error("No se pudo gestionar el fallo de {}; el mensaje permanece pendiente", record.getId(), e);
        }
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    public void recoverPending() {
        String stream = properties.getStreamName("job-applications", "stream:job-applications");
        try {
            PendingMessages pending = redis.opsForStream().pending(stream, group(), Range.unbounded(), 100);
            if (pending == null) return;
            for (var message : pending) {
                if (message.getElapsedTimeSinceLastDelivery().compareTo(MIN_IDLE) < 0) continue;
                var claimed = redis.<String, String>opsForStream().claim(stream, group(),
                        properties.getConsumer().getName(), MIN_IDLE, message.getId());
                if (claimed != null) claimed.forEach(this::onMessage);
            }
        } catch (Exception e) {
            log.warn("No se pudieron recuperar mensajes pendientes de {}: {}", stream, e.getMessage());
        }
    }
}
