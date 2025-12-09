package com.vsu.researchapp.service;

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
        // later we can enforce who created it, timestamps, etc.
        return repo.save(opportunity);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
