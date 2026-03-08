package com.respawn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReSpawnMarketApplication
{
    // load .env file

    void main()
    {
        SpringApplication.run(ReSpawnMarketApplication.class);
        IO.println("Running Spring Boot on JDK 25!");
    }

}
