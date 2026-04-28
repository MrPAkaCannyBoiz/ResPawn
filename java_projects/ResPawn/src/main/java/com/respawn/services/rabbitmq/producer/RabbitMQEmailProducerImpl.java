package com.respawn.services.rabbitmq.producer;

import com.respawn.dtos.WelcomeEmailDto;
import com.respawn.services.interfaces.EmailProducer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQEmailProducerImpl implements EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQEmailProducerImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendWelcomeEmailEvent(WelcomeEmailDto dto) {
        rabbitTemplate.convertAndSend("welcomeEmail", "", dto);
        System.out.println("Sent welcome email event to RabbitMQ for: " + dto.email());
    }
}
