package com.vsu.researchapp.service;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.repository.OpportunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResearchOpportunityService {

    private final OpportunityRepository opportunityRepository;

    // Constructor injection – Spring will give us the repository
    public ResearchOpportunityService(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    // === Methods your controller will call ===

    // GET /api/opportunities  -> list everything
    public List<ResearchOpportunity> getAllOpportunities() {
        return opportunityRepository.findAll();
    }

    // GET /api/opportunities/{id} -> get one by id
    public ResearchOpportunity getOpportunityById(Long id) {
        return opportunityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opportunity not found: " + id));
    }

    // POST /api/opportunities -> create new
    public ResearchOpportunity createOpportunity(ResearchOpportunity opportunity) {
        return opportunityRepository.save(opportunity);
    }

    // PUT /api/opportunities/{id} -> update existing
    public ResearchOpportunity updateOpportunity(Long id, ResearchOpportunity updated) {
        ResearchOpportunity existing = getOpportunityById(id);

        // copy fields from the updated object into the existing one
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setProfessor(updated.getProfessor());
        existing.setRequirements(updated.getRequirements());
        // add/remove fields here as you grow the model

        return opportunityRepository.save(existing);
    }

    // DELETE /api/opportunities/{id} -> delete by id
    public void deleteOpportunity(Long id) {
        opportunityRepository.deleteById(id);
    }
}
