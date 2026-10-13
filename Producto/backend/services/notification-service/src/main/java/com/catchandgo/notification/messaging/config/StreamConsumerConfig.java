package com.catchandgo.notification.messaging.config;

import com.catchandgo.notification.messaging.consumer.JobApplicationStreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.Collections;

/**
 * Configuración de contenedores de escucha para Redis Streams en notification-service.
 * Cumple con la declaración de Beans de configuración de la rúbrica.
 */
@Configuration
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
            JobApplicationStreamConsumer consumer) {

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
        Subscription subscription = container.receiveAutoAck(
            Consumer.from(groupName, consumerName),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed()),
            // El ACK se maneja manualmente dentro de consumer.onMessage() para mayor robustez
            record -> consumer.onMessage(record)
        );

        container.start();
        log.info("[STREAM-CONSUMER-START] Contenedor de escucha iniciado en stream '{}' | Grupo='{}' | Consumidor='{}'",
                streamKey, groupName, consumerName);

        return container;
    }

    private void ensureStreamAndGroup(StringRedisTemplate redisTemplate, String streamKey, String groupName) {
        try {
            Boolean hasKey = redisTemplate.hasKey(streamKey);
            if (Boolean.FALSE.equals(hasKey)) {
                redisTemplate.opsForStream().add(streamKey, Collections.singletonMap("system", "init"));
                log.info("[STREAM-INIT] Stream creado por notification-service: {}", streamKey);
            }

            try {
                redisTemplate.opsForStream().createGroup(streamKey, groupName);
                log.info("[STREAM-INIT] Consumer Group '{}' registrado en stream '{}'", groupName, streamKey);
            } catch (Exception e) {
                log.debug("[STREAM-INIT] Consumer Group '{}' ya registrado", groupName);
            }
        } catch (Exception e) {
            log.warn("[STREAM-INIT] Advertencia inicializando stream en notification-service: {}", e.getMessage());
        }
    }
}
