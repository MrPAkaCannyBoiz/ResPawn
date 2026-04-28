package com.respawn.services.rabbitmq.producer;

import com.respawn.dtos.TestDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQTestImpl
{
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQTestImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTestEvent() {
        rabbitTemplate.convertAndSend("test", "", new TestDto("respawn@test.com"));
        System.out.println("Sent test event to RabbitMQ");
    }
}
