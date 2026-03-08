package com.respawn.services.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public EmailProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendWelcomeEmailEvent(String emailAddress) {
        // send email address as message to welcomeEmail topic
        kafkaTemplate.send("welcomeEmail", emailAddress);
        String message = "Send welcome email to" + emailAddress;
        IO.println(message);
    }

    public void sendTestEvent() {
        kafkaTemplate.send("test","Kafka is work!!!!");
        IO.println("Send test event");
    }
}
