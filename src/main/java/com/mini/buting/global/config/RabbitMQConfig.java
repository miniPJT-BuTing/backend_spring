package com.mini.buting.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    private String RABBIT_MQ_HOST;
    @Value("${spring.rabbitmq.port}")
    private Integer RABBIT_MQ_PORT;
    @Value("${spring.rabbitmq.username}")
    private String RABBIT_MQ_USERNAME;
    @Value("${spring.rabbitmq.password}")
    private String RABBIT_MQ_PASSWORD;
    @Value("${spring.rabbitmq.virtual-host}")
    private String RABBIT_MQ_VHOST;
    @Value("${rabbitmq.chat-queue.name}")
    private String CHAT_QUEUE_NAME;
    @Value("${rabbitmq.chat-exchange.name}")
    private String CHAT_EXCHANGE_NAME;
    @Value("${rabbitmq.chat-routing.key}")
    private String CHAT_ROUTING_KEY;

    // "chat.queue"라는 이름의 Queue 생성
    @Bean
    public Queue chatQueue() {
        return new Queue(CHAT_QUEUE_NAME, true); // durable을 true로 제공
    }

    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE_NAME);
    }

    @Bean
    public Binding chatBinding(Queue chatQueue, TopicExchange chatExchange) {
        return BindingBuilder
                .bind(chatQueue)
                .to(chatExchange)
                .with(CHAT_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public RabbitMessagingTemplate rabbitMessagingTemplate(RabbitTemplate rabbitTemplate) {
        return new RabbitMessagingTemplate(rabbitTemplate);
    }

    @Bean
    public ConnectionFactory createConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(RABBIT_MQ_HOST);
        factory.setUsername(RABBIT_MQ_USERNAME);
        factory.setPassword(RABBIT_MQ_PASSWORD);
        factory.setPort(RABBIT_MQ_PORT);
        factory.setVirtualHost(RABBIT_MQ_VHOST);

        return factory;
    }

    // Queue를 구독(Subscribe)하는 걸 어떻게 처리하느냐에 따라 필요함. 당장은 없어도 됨.
    /*@Bean
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }*/

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    @Bean
    public ApplicationRunner rabbitAdminInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
