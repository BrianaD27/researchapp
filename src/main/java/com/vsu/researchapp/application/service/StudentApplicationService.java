package com.vsu.researchapp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.StudentApplication;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import com.vsu.researchapp.domain.repository.StudentApplicationRepository;
import com.vsu.researchapp.infrastructure.security.AuthorizationService;

@Service
public class StudentApplicationService {

    private final StudentApplicationRepository appRepo;
    private final ResearchOpportunityRepository oppRepo;
    private final AuthorizationService authz;

    public StudentApplicationService(StudentApplicationRepository appRepo,
                                     ResearchOpportunityRepository oppRepo,
                                     AuthorizationService authz) {
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
        this.authz = authz;
    }

    // Called by your controller's create()
    public StudentApplication applyToOpportunity(Long opportunityId, StudentApplication input) {

        //  #8: enforce identity (prevents spoofing studentEmail)
        String currentUser = authz.currentUsername();
        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        // Find the opportunity or fail
        ResearchOpportunity opp = oppRepo.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + opportunityId));

        // Build the new application record
        StudentApplication app = new StudentApplication();
        app.setOpportunity(opp);

        // Keep name/message from input
        app.setStudentName(input.getStudentName());
        app.setMessage(input.getMessage());

        //  Force email owner to the logged-in user
        app.setStudentEmail(currentUser);

        return appRepo.save(app);
    }

    // Called by your controller's list()
    public List<StudentApplication> getApplicationsByOpportunity(Long opportunityId) {

        //  Admin can see all applications for that opportunity
        if (authz.isAdmin()) {
            return appRepo.findByOpportunityId(opportunityId);
        }

        //  Student can only see THEIR applications
        String currentUser = authz.currentUsername();
        if (currentUser == null) {
            throw new RuntimeException("Unauthorized");
        }

        return appRepo.findByOpportunityIdAndStudentEmailIgnoreCase(opportunityId, currentUser);
    }

    // Admin use only (should be protected in controller with hasRole ADMIN)
    public List<StudentApplication> getAllApplications() {
        return appRepo.findAll();
    }

    // Admin use only (should be protected in controller with hasRole ADMIN)
    public StudentApplication updateStatus(Long applicationId, String status) {
        StudentApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        app.setStatus(status.toUpperCase());
        return appRepo.save(app);
    }
}
