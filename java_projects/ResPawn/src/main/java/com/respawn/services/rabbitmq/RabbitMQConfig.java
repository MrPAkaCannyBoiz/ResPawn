package com.respawn.services.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WELCOME_EMAIL_EXCHANGE = "welcomeEmail";
    public static final String WELCOME_EMAIL_QUEUE = "welcomeEmail";
    public static final String TEST_EXCHANGE = "test";
    public static final String TEST_QUEUE = "test";

    @Bean
    public FanoutExchange welcomeEmailExchange() {
        return new FanoutExchange(WELCOME_EMAIL_EXCHANGE);
    }

    @Bean
    public Queue welcomeEmailQueue() {
        return QueueBuilder.durable(WELCOME_EMAIL_QUEUE).build();
    }

    @Bean
    public Binding welcomeEmailBinding(Queue welcomeEmailQueue, FanoutExchange welcomeEmailExchange) {
        return BindingBuilder.bind(welcomeEmailQueue).to(welcomeEmailExchange);
    }

    @Bean
    public FanoutExchange testExchange() {
        return new FanoutExchange(TEST_EXCHANGE);
    }

    @Bean
    public Queue testQueue() {
        return QueueBuilder.durable(TEST_QUEUE).build();
    }

    @Bean
    public Binding testBinding(Queue testQueue, FanoutExchange testExchange) {
        return BindingBuilder.bind(testQueue).to(testExchange);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
