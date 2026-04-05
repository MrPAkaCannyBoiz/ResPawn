package com.respawn.services;

import com.respawn.dtos.WelcomeEmailDto;
import com.respawn.services.kafka.producer.services.EmailProducerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailProducerImplTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private EmailProducerImpl emailProducer;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        emailProducer = new EmailProducerImpl(kafkaTemplate, objectMapper);
    }

    @Test
    void sendWelcomeEmailEvent_validDto_sendsJsonToWelcomeEmailTopic() {
        // WelcomeEmailDto record: (firstname, lastname, email)
        WelcomeEmailDto dto = new WelcomeEmailDto("John", "Doe", "john@example.com");

        emailProducer.sendWelcomeEmailEvent(dto);

        verify(kafkaTemplate, times(1)).send(eq("welcomeEmail"), anyString());
    }

    @Test
    void sendTestEvent_sendsToTestTopic() {
        emailProducer.sendTestEvent();

        verify(kafkaTemplate, times(1)).send(eq("test"), anyString());
    }

    @Test
    void sendWelcomeEmailEvent_kafkaException_throwsRuntimeException() {
        WelcomeEmailDto dto = new WelcomeEmailDto("Jane", "Smith", "jane@example.com");
        when(kafkaTemplate.send(anyString(), anyString()))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> emailProducer.sendWelcomeEmailEvent(dto));
        assertEquals("Unexpected exception occurred", ex.getMessage());
    }
}
