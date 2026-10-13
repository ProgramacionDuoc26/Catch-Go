package com.catchandgo.jobs.messaging.producer;

import com.catchandgo.common.events.JobApplicationEvent;
import com.catchandgo.jobs.messaging.config.MessagingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Publicador asíncrono de eventos de postulación en Redis Streams.
 * Garantiza durabilidad (persistencia en cola), serialización JSON y aplicación de políticas de retención.
 */
@Service
public class JobApplicationStreamPublisher {

    private static final Logger log = LoggerFactory.getLogger(JobApplicationStreamPublisher.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MessagingProperties messagingProperties;

    public JobApplicationStreamPublisher(StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper,
                                        MessagingProperties messagingProperties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messagingProperties = messagingProperties;
    }

    /**
     * Publica un evento de postulación en la cola de mensajes persistente.
     * @param event Evento con los datos de la postulación
     * @return RecordId asignado por Redis al mensaje
     */
    public RecordId publish(JobApplicationEvent event) {
        String streamKey = messagingProperties.getStreamName("job-applications", "stream:job-applications");
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            Map<String, String> fields = Map.of(
                "eventId", event.eventId(),
                "type", "JOB_APPLICATION_SUBMITTED",
                "applicationId", String.valueOf(event.applicationId()),
                "offerId", String.valueOf(event.offerId()),
                "candidateId", event.candidateId(),
                "employerId", event.employerId(),
                "timestamp", event.timestamp().toString(),
                "payload", payloadJson
            );

            MapRecord<String, String, String> record = MapRecord.create(streamKey, fields);
            RecordId recordId = redisTemplate.opsForStream().add(record);

            // Aplicar política de retención (MAXLEN ~)
            Long maxLen = messagingProperties.getRetention().getMaxLen();
            if (maxLen != null && maxLen > 0) {
                redisTemplate.opsForStream().trim(streamKey, maxLen, true);
            }

            log.info("[ASYNC-MQ PRODUCER] Mensaje persistido en stream '{}' ID='{}' | Candidato='{}' -> Empleo='{}'",
                    streamKey, recordId, event.candidateId(), event.offerTitle());
            return recordId;
        } catch (Exception e) {
            log.error("[ASYNC-MQ ERROR] Error al publicar en stream '{}': {}", streamKey, e.getMessage(), e);
            throw new RuntimeException("Error en broker al publicar mensaje asíncrono: " + e.getMessage(), e);
        }
    }
}
