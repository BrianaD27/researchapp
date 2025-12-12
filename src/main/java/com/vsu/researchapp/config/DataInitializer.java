package com.vsu.researchapp.config;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserAccountRepository userRepo,
                               ResearchOpportunityRepository oppRepo,
                               PasswordEncoder encoder) {
        return args -> {

            // ---- ADMIN USER SEED ----
            if (userRepo.findByUsername("admin123").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin123");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(encoder.encode("admin123"));
                // if your UserAccount has a role field, keep this. If not, you can comment it out.
                try {
                    admin.setRole("ADMIN");
                } catch (NoSuchMethodError | RuntimeException e) {
                    System.out.println("Role field not present on UserAccount (that's okay for now).");
                }
                userRepo.save(admin);
                System.out.println("Admin user created: admin123 / admin123");
            }

            // ---- RESEARCH OPPORTUNITIES SEED ----
            if (oppRepo.count() == 0) {
                ResearchOpportunity opp1 = new ResearchOpportunity();
                opp1.setTitle("Machine Learning in Bioinformatics");
                opp1.setDescription("Undergraduate research using Python and ML to analyze genomic data.");
                opp1.setProfessor("Dr. Daniels");
                opp1.setRequirements("Python, basic statistics, interest in biology.");

                ResearchOpportunity opp2 = new ResearchOpportunity();
                opp2.setTitle("IoT Security for Smart Campus Devices");
                opp2.setDescription("Research project securing IoT sensors in the VSU engineering building.");
                opp2.setProfessor("Dr. Waller");
                opp2.setRequirements("Networking basics, interest in cybersecurity.");

                oppRepo.save(opp1);
                oppRepo.save(opp2);

                System.out.println("Seeded 2 real ResearchOpportunity records.");
            }
        };
    }
}