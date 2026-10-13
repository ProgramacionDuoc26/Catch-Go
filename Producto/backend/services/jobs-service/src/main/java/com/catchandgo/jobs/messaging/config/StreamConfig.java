package com.catchandgo.jobs.messaging.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;

/**
 * Clase de configuración que declara beans de infraestructura de mensajería Redis Streams.
 * Cumple con el requerimiento de separación de lógica de negocio y configuración declarativa de colas.
 */
@Configuration
public class StreamConfig {

    private static final Logger log = LoggerFactory.getLogger(StreamConfig.class);

    private final MessagingProperties messagingProperties;

    public StreamConfig(MessagingProperties messagingProperties) {
        this.messagingProperties = messagingProperties;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * Bean inicializador que asegura la existencia de los Streams y Consumer Groups configurados.
     */
    @Bean
    public CommandLineRunner initRedisStreams(StringRedisTemplate redisTemplate) {
        return args -> {
            String jobStream = messagingProperties.getStreamName("job-applications", "stream:job-applications");
            String notifGroup = messagingProperties.getGroupName("notifications-group", "notifications-group");
            String dlqStream = messagingProperties.getStreamName("dlq", "stream:job-applications:dlq");

            initStreamAndGroup(redisTemplate, jobStream, notifGroup);
            initStream(redisTemplate, dlqStream);
        };
    }

    private void initStreamAndGroup(StringRedisTemplate redisTemplate, String streamKey, String groupName) {
        try {
            Boolean hasKey = redisTemplate.hasKey(streamKey);
            if (Boolean.FALSE.equals(hasKey)) {
                // Crear una entrada inicial para instanciar el stream
                RecordId initId = redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("system", "init"));
                log.info("[STREAM-INIT] Stream creado: {} con ID={}", streamKey, initId);
            }

            try {
                // Crear el grupo de consumidores si no existe
                redisTemplate.opsForStream().createGroup(streamKey, groupName);
                log.info("[STREAM-INIT] Consumer Group '{}' creado en stream '{}'", groupName, streamKey);
            } catch (Exception e) {
                // Si el grupo ya existe (BUSYGROUP), se ignora de forma segura
                log.debug("[STREAM-INIT] Grupo '{}' ya existe en stream '{}': {}", groupName, streamKey, e.getMessage());
            }
        } catch (Exception e) {
            log.warn("[STREAM-INIT] Advertencia al inicializar stream '{}': {}", streamKey, e.getMessage());
        }
    }

    private void initStream(StringRedisTemplate redisTemplate, String streamKey) {
        try {
            Boolean hasKey = redisTemplate.hasKey(streamKey);
            if (Boolean.FALSE.equals(hasKey)) {
                redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("system", "dlq-init"));
                log.info("[STREAM-INIT] DLQ Stream creado: {}", streamKey);
            }
        } catch (Exception e) {
            log.warn("[STREAM-INIT] Advertencia al inicializar DLQ stream '{}': {}", streamKey, e.getMessage());
        }
    }
}
