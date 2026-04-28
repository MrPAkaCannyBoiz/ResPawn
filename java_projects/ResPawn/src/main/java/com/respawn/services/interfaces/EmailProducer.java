package com.respawn.services.interfaces;

import com.respawn.dtos.WelcomeEmailDto;

public interface EmailProducer
{
    public void sendWelcomeEmailEvent(WelcomeEmailDto emailDto);
}
