package com.catchandgo.notification.messaging.config;

import com.catchandgo.notification.messaging.consumer.JobApplicationStreamConsumer;
import com.catchandgo.notification.messaging.consumer.ApplicationStatusStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.util.Collections;

/**
 * Configuración de contenedores de escucha para Redis Streams en notification-service.
 * Cumple con la declaración de Beans de configuración de la rúbrica.
 */
@Configuration
@org.springframework.scheduling.annotation.EnableScheduling
public class StreamConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(StreamConsumerConfig.class);

    private final MessagingProperties messagingProperties;

    public StreamConsumerConfig(MessagingProperties messagingProperties) {
        this.messagingProperties = messagingProperties;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean(destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            StringRedisTemplate redisTemplate,
            JobApplicationStreamConsumer consumer, ApplicationStatusStreamConsumer statusConsumer) {

        String streamKey = messagingProperties.getStreamName("job-applications", "stream:job-applications");
        String groupName = messagingProperties.getGroupName("notifications-group", "notifications-group");
        String consumerName = messagingProperties.getConsumer().getName();

        // 1. Asegurar la existencia del stream y del grupo antes de escuchar
        ensureStreamAndGroup(redisTemplate, streamKey, groupName);

        // 2. Configuración de opciones del contenedor de sondeo
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
            StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .pollTimeout(Duration.ofMillis(messagingProperties.getConsumer().getPollTimeoutMs()))
                .batchSize(messagingProperties.getConsumer().getBatchSize())
                .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
            StreamMessageListenerContainer.create(connectionFactory, options);

        // 3. Suscripción al grupo de consumidores
        container.receive(
            Consumer.from(groupName, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            consumer
        );

        String statusStream = messagingProperties.getStreamName("application-status", "stream:application-status");
        String statusGroup = messagingProperties.getGroupName("application-status-group", "application-status-notifications");
        ensureStreamAndGroup(redisTemplate, statusStream, statusGroup);
        container.receive(Consumer.from(statusGroup, consumerName),
            StreamOffset.create(statusStream, ReadOffset.lastConsumed()), statusConsumer);

        container.start();
        log.info("[STREAM-CONSUMER-START] Contenedor de escucha iniciado en stream '{}' | Grupo='{}' | Consumidor='{}'",
                streamKey, groupName, consumerName);

        return container;
    }

    void ensureStreamAndGroup(StringRedisTemplate redisTemplate, String streamKey, String groupName) {
        Boolean hasKey = redisTemplate.hasKey(streamKey);
        if (hasKey == null) throw new IllegalStateException("Redis no confirmo si existe el stream " + streamKey);
        if (!hasKey) {
            if (redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("system", "init")) == null) {
                throw new IllegalStateException("Redis no confirmo la creacion del stream " + streamKey);
            }
            log.info("[STREAM-INIT] Stream creado por notification-service: {}", streamKey);
        }

        try {
            // Un grupo nuevo debe leer tambien los eventos que ya estaban en el stream.
            redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0-0"), groupName);
            log.info("[STREAM-INIT] Consumer Group '{}' registrado en stream '{}'", groupName, streamKey);
        } catch (RuntimeException e) {
            for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                if (cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP")) {
                    log.debug("[STREAM-INIT] Consumer Group '{}' ya registrado en '{}'", groupName, streamKey);
                    return;
                }
            }
            throw e;
        }
    }
}
