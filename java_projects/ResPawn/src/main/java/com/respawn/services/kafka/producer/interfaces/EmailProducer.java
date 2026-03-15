package com.respawn.services.kafka.producer.interfaces;

public interface EmailProducer
{
    public void sendWelcomeEmailEvent(String emailAddress);
    public void sendTestEvent();
}
