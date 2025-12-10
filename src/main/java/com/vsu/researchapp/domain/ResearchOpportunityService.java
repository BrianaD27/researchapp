package com.vsu.researchapp.domain;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResearchOpportunityService {

    private final ResearchOpportunityRepository repo;

    public ResearchOpportunityService(ResearchOpportunityRepository repo) {
        this.repo = repo;
    }

    public List<ResearchOpportunity> getAll() {
        return repo.findAll();
    }

    public ResearchOpportunity getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Research opportunity not found: " + id));
    }

    public ResearchOpportunity create(ResearchOpportunity opportunity) {
        return repo.save(opportunity);
    }

    public ResearchOpportunity update(Long id, ResearchOpportunity updated) {
        ResearchOpportunity existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setProfessor(updated.getProfessor());
        existing.setRequirements(updated.getRequirements());
        return repo.save(existing);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
