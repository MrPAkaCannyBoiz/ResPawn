package com.respawn.services.kafka.consumer;

import org.aspectj.weaver.SignatureUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailConsumer
{
    private MailSender mailSender;
    private SimpleMailMessage simpleMailMessage;

    public void setMailSender(MailSender mailSender)
    {
        this.mailSender = mailSender;
    }

    public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage)
    {
        this.simpleMailMessage = simpleMailMessage;
    }

    // LISTENER: Waits for a message on "welcomeEmail" topic.
    // The generic type matches the Producer (String -> String).
    // passes it into the 'customerEmail' parameter.
    @KafkaListener(id = "email", topics = "welcomeEmail")
    public void sendEmail(String emailAddress) // need argument like dto
    {
        if (emailAddress == null || emailAddress.isEmpty())
        {
            System.err.println("Received empty email address from Kafka.");
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage(simpleMailMessage);
        message.setTo(emailAddress);
        message.setText("Welcome to ResPawn! Your registration was successful.");
        try
        {
            mailSender.send(message);
        }
        catch (MailException ex)
        {
            IO.println(ex.getMessage());
        }
    }

}
