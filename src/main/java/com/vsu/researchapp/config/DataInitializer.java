package com.vsu.researchapp.config;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserAccountRepository repo,
                                PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("admin123").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin123");
                admin.setPasswordHash(encoder.encode("admin123"));
                repo.save(admin);
                System.out.println("Admin user created: admin123 / admin123");
            }
        };
    }
}
