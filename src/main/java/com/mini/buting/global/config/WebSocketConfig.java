package com.mini.buting.global.config;

import com.mini.buting.api.chat.interceptor.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtChannelInterceptor jwtChannelInterceptor;
    @Value("${spring.rabbitmq.host}")
    private String RABBIT_MQ_HOST;
    @Value("${spring.rabbitmq.username}")
    private String RABBIT_MQ_USERNAME;
    @Value("${spring.rabbitmq.password}")
    private String RABBIT_MQ_PASSWORD;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange", "/amq/queue")
                .setRelayHost(RABBIT_MQ_HOST)
                .setRelayPort(61613)
                .setClientLogin(RABBIT_MQ_USERNAME)
                .setClientPasscode(RABBIT_MQ_PASSWORD)
                .setSystemLogin(RABBIT_MQ_USERNAME)
                .setSystemPasscode(RABBIT_MQ_PASSWORD);

        registry.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

}
