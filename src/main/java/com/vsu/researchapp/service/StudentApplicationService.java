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

    public StudentApplication applyToOpportunity(Long opportunityId, StudentApplication input) {
        ResearchOpportunity opp = oppRepo.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + opportunityId));

        StudentApplication app = new StudentApplication();
        app.setOpportunity(opp);
        app.setStudentName(input.getStudentName());
        app.setStudentEmail(input.getStudentEmail());
        app.setMessage(input.getMessage());
        app.setStatus("PENDING");

        return appRepo.save(app);
    }

    public List<StudentApplication> getApplicationsForOpportunity(Long opportunityId) {
        ResearchOpportunity opp = oppRepo.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + opportunityId));
        return appRepo.findByOpportunity(opp);
    }

    public StudentApplication getById(Long id) {
        return appRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + id));
    }

    public StudentApplication updateStatus(Long appId, String status) {
        StudentApplication app = getById(appId);
        app.setStatus(status);
        return appRepo.save(app);
    }
}
