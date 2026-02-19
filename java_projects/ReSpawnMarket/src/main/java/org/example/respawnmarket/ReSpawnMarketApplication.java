package org.example.respawnmarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReSpawnMarketApplication
{
    // load .env file

    public static void main(String[] args)
    {
        SpringApplication.run(ReSpawnMarketApplication.class, args);
        IO.println("Running Spring Boot on JDK 25 but with JDK 21 syntax");
    }

}
