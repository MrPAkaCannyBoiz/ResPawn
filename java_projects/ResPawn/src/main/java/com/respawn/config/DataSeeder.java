package com.respawn.config;

import com.respawn.entities.ResellerEntity;
import com.respawn.repositories.ResellerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements ApplicationRunner {
    private final ResellerRepository resellerRepository;

    @Value("${SEED_RESELLER_USERNAME:}")
    private String seedUsername;

    @Value("${SEED_RESELLER_PASSWORD:}")
    private String seedPassword;

    @Value("${SEED_RESELLER_NAME:}")
    private String seedName;

    public DataSeeder(ResellerRepository resellerRepository) {
        this.resellerRepository = resellerRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (seedUsername.isBlank()) return;
        if (resellerRepository.count() > 0) return;

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        ResellerEntity reseller = new ResellerEntity();
        reseller.setName(seedName);
        reseller.setUsername(seedUsername);
        reseller.setPassword(encoder.encode(seedPassword));
        resellerRepository.save(reseller);
        System.out.println("Seeded default reseller: " + seedUsername);
    }
}
