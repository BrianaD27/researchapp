package com.vsu.researchapp.service;

import com.vsu.researchapp.domain.model.StudentApplication;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
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

    public StudentApplication applyToOpportunity(Long opportunityId, StudentApplication appInput) {
        ResearchOpportunity opp = oppRepo.findById(opportunityId)
                .orElseThrow(() -> new IllegalArgumentException("Opportunity not found: " + opportunityId));

        appInput.setOpportunity(opp);
        return appRepo.save(appInput);
    }

    public List<StudentApplication> getApplicationsByOpportunity(Long opportunityId) {
        return appRepo.findByOpportunityId(opportunityId);
    }
}

