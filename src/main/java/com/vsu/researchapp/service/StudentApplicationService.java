package com.vsu.researchapp.service;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.StudentApplication;
import com.vsu.researchapp.domain.repository.StudentApplicationRepository;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentApplicationService {

    private final StudentApplicationRepository appRepo;
    private final ResearchOpportunityRepository oppRepo;

    public StudentApplicationService(StudentApplicationRepository appRepo,
                                     ResearchOpportunityRepository oppRepo) {
        this.appRepo = appRepo;
        this.oppRepo = oppRepo;
    }

    // Called by your controller's create()
    public StudentApplication applyToOpportunity(Long opportunityId, StudentApplication input) {
        // Find the opportunity or fail
        ResearchOpportunity opp = oppRepo.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + opportunityId));

        // Build the new application record
        StudentApplication app = new StudentApplication();
        app.setOpportunity(opp);

        // These must match the fields in StudentApplication.java
        app.setStudentName(input.getStudentName());
        app.setStudentEmail(input.getStudentEmail());
        app.setMessage(input.getMessage());

        return appRepo.save(app);
    }

    // Called by your controller's list()
       public List<StudentApplication> getApplicationsByOpportunity(Long opportunityId) {
        return appRepo.findByOpportunityId(opportunityId);
    }

    public List<StudentApplication> getAllApplications() {
        return appRepo.findAll();
    }

    public StudentApplication updateStatus(Long applicationId, String status) {
        StudentApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));

        app.setStatus(status.toUpperCase());
        return appRepo.save(app);
    }
}
