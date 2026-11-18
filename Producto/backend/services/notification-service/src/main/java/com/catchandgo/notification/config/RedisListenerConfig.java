package com.catchandgo.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import com.catchandgo.notification.hub.NotificationHandler;
import com.catchandgo.notification.hub.NotificationBroadcaster;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@SuppressWarnings("null")
public class RedisListenerConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic(NotificationBroadcaster.CHANNEL));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(NotificationHandler handler) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(handler, "handleMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }
}
