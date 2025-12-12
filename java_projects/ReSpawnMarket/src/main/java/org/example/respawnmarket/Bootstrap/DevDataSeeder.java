package org.example.respawnmarket.Bootstrap;

import org.example.respawnmarket.entities.ResellerEntity;
import org.example.respawnmarket.repositories.ResellerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DevDataSeeder implements CommandLineRunner {

    private final ResellerRepository resellerRepository;

    public DevDataSeeder(ResellerRepository resellerRepository) {
        this.resellerRepository = resellerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedReseller();
    }

    private void seedReseller() {
        if (resellerRepository.count() > 0) {
            System.out.println("[DevDataSeeder] Resellers already exist, skipping seeding.");
            return;
        }

        ResellerEntity reseller = new ResellerEntity();
        reseller.setName("Test Reseller");
        reseller.setUsername("test_reseller");

        // parola clară = "1234"
        String hashedPassword = BCrypt.hashpw("1234", BCrypt.gensalt(10));
        reseller.setPassword(hashedPassword);

        resellerRepository.save(reseller);

        System.out.println("[DevDataSeeder] Seeded default reseller (username: test_reseller, password: 1234)");
    }
}
