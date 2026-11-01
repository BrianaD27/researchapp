package com.vsu.researchapp.infrastructure.config;

import java.util.List;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserAccountRepository userRepo,
                               ResearchOpportunityRepositoryInterface oppRepo, ProfessorRepositoryInterface proRepo,
                               PasswordEncoder encoder,
                               @Value("${bootstrap.admin.username:}") String adminUsername,
                               @Value("${bootstrap.admin.email:}") String adminEmail,
                               @Value("${bootstrap.admin.password:}") String adminPassword,
                               @Value("${app.seed-demo-data:false}") boolean seedDemoData) {
        return args -> {

            // ---- FIRST ADMIN ----
            // There is no built-in admin password. On a brand-new database with no
            // admin, set BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_EMAIL, and
            // BOOTSTRAP_ADMIN_PASSWORD once to create the first admin, then remove them.
            boolean anyAdmin = userRepo.findAll().stream()
                .anyMatch(u -> "ADMIN".equalsIgnoreCase(u.getRole()));
            if (!anyAdmin && !adminUsername.isBlank() && !adminEmail.isBlank() && adminPassword.length() >= 12
                    && userRepo.findByUsername(adminUsername.trim()).isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername(adminUsername.trim());
                admin.setEmail(adminEmail.trim());
                admin.setPasswordHash(encoder.encode(adminPassword));
                admin.setRole("ADMIN");
                userRepo.save(admin);
                System.out.println("First admin account created: " + admin.getUsername());
            } else if (!anyAdmin) {
                System.out.println("No admin account yet. Set BOOTSTRAP_ADMIN_USERNAME, BOOTSTRAP_ADMIN_EMAIL, "
                    + "and BOOTSTRAP_ADMIN_PASSWORD (12+ characters) once to create one.");
            }
            // ---- PROFESSOR OPPORTUNITIES SEED ----
            Professor drDaniels = null;
            Professor drWaller = null;

            // Sample professors and projects only when app.seed-demo-data=true, so a
            // real database never gets placeholder data.
            if (seedDemoData && proRepo.getAllProfessors().isEmpty()) {
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
