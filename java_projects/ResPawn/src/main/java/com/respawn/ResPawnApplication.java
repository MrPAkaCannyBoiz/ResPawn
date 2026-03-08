package com.respawn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResPawnApplication
{
    // load .env file

    static void main(String[] args)
    {
        SpringApplication.run(ResPawnApplication.class, args);
        IO.println("Running Spring Boot on JDK 25!");
    }

}
