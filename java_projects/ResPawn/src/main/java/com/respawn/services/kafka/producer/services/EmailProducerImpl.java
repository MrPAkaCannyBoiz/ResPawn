package com.respawn.services.kafka.producer.services;

import com.respawn.dtos.WelcomeEmailDto;
import com.respawn.services.kafka.producer.interfaces.EmailProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class EmailProducerImpl implements EmailProducer
{

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public EmailProducerImpl(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendWelcomeEmailEvent(WelcomeEmailDto dto) {
        try {
            // send email address as message to welcomeEmail topic with JSON payload
            String jsonPayload = objectMapper.writeValueAsString(dto);
            kafkaTemplate.send("welcomeEmail", jsonPayload);
            String message = "Send welcome email to" + dto.email();
            IO.println(message);
        } catch (JacksonException e) {
            throw new RuntimeException("Failed to serialize DTO to JSON", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception occurred", e);
        }
    }

    public void sendTestEvent() {
        kafkaTemplate.send("test","respawn@test.com");
        IO.println("Send test event");
    }
}
