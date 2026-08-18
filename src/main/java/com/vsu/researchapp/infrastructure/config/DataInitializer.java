package com.vsu.researchapp.infrastructure.config;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.repository.ProfessorRepository;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Value("${app.dev.admin-password:}")
    private String developmentAdminPassword;

    @Bean
    CommandLineRunner initData(UserAccountRepository userRepo,
                               ResearchOpportunityRepository oppRepo, ProfessorRepository proRepo,
                               PasswordEncoder encoder) {
        return args -> {

            // ---- ADMIN USER SEED ----
            if (!developmentAdminPassword.isBlank()
                    && userRepo.findByUsername("admin123").isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin123");
                admin.setEmail("admin@example.com");
                admin.setPasswordHash(encoder.encode(
                    developmentAdminPassword));
                admin.setRole("ROLE_ADMIN");
                userRepo.save(admin);
            }
            // ---- PROFESSOR OPPORTUNITIES SEED ----
            Professor drDaniels = null;
            Professor drWaller = null;

            if (proRepo.count() == 0) {
                drDaniels = new Professor();
                drDaniels.setName("Dr. Daniels");
                drDaniels.setEmail("daniels@vsu.edu");
                drDaniels.setDepartment("Computer Science");
                drDaniels.setTitle("Professor");
                proRepo.save(drDaniels);

                drWaller = new Professor();
                drWaller.setName("Dr. Waller");
                drWaller.setEmail("waller@vsu.edu");
                drWaller.setDepartment("Engineering");
                drDaniels.setTitle("Professor");
                proRepo.save(drWaller);
            }

            // ---- RESEARCH OPPORTUNITIES SEED ----
            if (oppRepo.count() == 0) {
                ResearchOpportunity opp1 = new ResearchOpportunity();
                opp1.setTitle("Machine Learning in Bioinformatics");
                opp1.setDescription("Undergraduate research using Python and ML to analyze genomic data.");
                opp1.setCreatedBy(drDaniels);
                opp1.setRequirements("Python, basic statistics, interest in biology.");

                ResearchOpportunity opp2 = new ResearchOpportunity();
                opp2.setTitle("IoT Security for Smart Campus Devices");
                opp2.setDescription("Research project securing IoT sensors in the VSU engineering building.");
                opp2.setCreatedBy(drWaller);
                opp2.setRequirements("Networking basics, interest in cybersecurity.");

                oppRepo.save(opp1);
                oppRepo.save(opp2);

                System.out.println("Seeded 2 real ResearchOpportunity records.");
            }
        };
    }
}
