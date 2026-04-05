package com.respawn.services.kafka.producer.interfaces;

import com.respawn.dtos.WelcomeEmailDto;

public interface EmailProducer
{
    public void sendWelcomeEmailEvent(WelcomeEmailDto emailDto);
    public void sendTestEvent();
}
