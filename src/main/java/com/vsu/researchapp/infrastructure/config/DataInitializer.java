package com.vsu.researchapp.infrastructure.config;

import java.util.List;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserAccountRepository userRepo,
                               ResearchOpportunityRepositoryInterface oppRepo, ProfessorRepositoryInterface proRepo,
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
            // ---- PROFESSOR OPPORTUNITIES SEED ----
            Professor drDaniels = null;
            Professor drWaller = null;

            if (proRepo.getAllProfessors().isEmpty()) {
                drDaniels = new Professor();
                drDaniels.setName("Dr. Daniels");
                drDaniels.setEmail("daniels@vsu.edu");
                drDaniels.setDepartment("Computer Science");
                drDaniels = proRepo.createProfessor(drDaniels);

                drWaller = new Professor();
                drWaller.setName("Dr. Waller");
                drWaller.setEmail("waller@vsu.edu");
                drWaller.setDepartment("Engineering");
                drWaller = proRepo.createProfessor(drWaller);
            }

            // ---- RESEARCH OPPORTUNITIES SEED ----
            if (drDaniels != null && drWaller != null && oppRepo.getAllResearchOpportunities().isEmpty()) {
                ResearchOpportunity opp1 = new ResearchOpportunity();
                opp1.setTitle("Machine Learning in Bioinformatics");
                opp1.setDescription("Undergraduate research using Python and ML to analyze genomic data.");
                opp1.setDepartment("Computer Science");
                opp1.setProfessor(drDaniels);
                opp1.setRequiredMajors(List.of("Computer Science", "Biology"));
                opp1.setRequiredSkills(List.of("Python", "Basic statistics"));

                ResearchOpportunity opp2 = new ResearchOpportunity();
                opp2.setTitle("IoT Security for Smart Campus Devices");
                opp2.setDescription("Research project securing IoT sensors in the VSU engineering building.");
                opp2.setDepartment("Engineering");
                opp2.setProfessor(drWaller);
                opp2.setRequiredMajors(List.of("Computer Science", "Electrical Engineering"));
                opp2.setRequiredSkills(List.of("Networking basics", "Interest in cybersecurity"));

                oppRepo.createResearchOpportunity(opp1);
                oppRepo.createResearchOpportunity(opp2);

                System.out.println("Seeded 2 real ResearchOpportunity records.");
            }
        };
    }
}
