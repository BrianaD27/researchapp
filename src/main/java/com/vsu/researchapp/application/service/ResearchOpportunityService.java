package com.vsu.researchapp.application.service;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepository;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.vsu.researchapp.application.dto.CreateResearchOpportunityDto;
import com.vsu.researchapp.application.dto.ResearchOpportunityDto;
import com.vsu.researchapp.application.dto.UpdateResearchOpportunityDto;

@Service
public class ResearchOpportunityService {

    private final ResearchOpportunityRepository repo;
    private final ProfessorRepository professorRepo;

    public ResearchOpportunityService(ResearchOpportunityRepository repo, ProfessorRepository professorRepo) {
        this.repo = repo;
        this.professorRepo = professorRepo;
    }

    public List<ResearchOpportunityDto> getAllResearchOpportunities() {
        List<ResearchOpportunity> opportunities = repo.findAll();
         return opportunities.stream().map(this::entityToDto).toList();
    }

    public ResearchOpportunityDto getResearchOpportunityById(Long id) {
        ResearchOpportunity opportunity = repo.findById(id).orElseThrow(() -> new RuntimeException("There is no Research Opportunity with the id: " + id));
        return entityToDto(opportunity);
    }

    public List<ResearchOpportunityDto> getResearchOpportunitiesByUpcoming() {
        List<ResearchOpportunity> opportunities = repo.getResearchOpportunitiesByUpcoming();
        return opportunities.stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> getResearchOpportunitiesByDateRange(LocalDate earliestDate, LocalDate latestDate) {
        List<ResearchOpportunity> opportunities = repo.getResearchOpportunitiesByDateRange(earliestDate, latestDate);

        return opportunities.stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> search(String term) {
        List<ResearchOpportunity> opportunities = repo.searchOpportunities(term);

        return opportunities.stream().map(this::entityToDto).toList();
    }

    public ResearchOpportunityDto createResearchOpportunity(CreateResearchOpportunityDto opportunity, Long professorId) {
        Professor professor = professorRepo.findById(professorId).orElseThrow(() -> new RuntimeException("There is no Professor with the id: " + professorId));

        //Validate Date
        if (opportunity.beginDate().isAfter(opportunity.endDate())) {
            throw new IllegalArgumentException("The beginning date must be before the end date");
        }
        if (opportunity.beginDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The beginning date must be after the today's date: " + LocalDate.now());
        }

        ResearchOpportunity newOpportunity = new ResearchOpportunity();
        newOpportunity.setTitle(opportunity.title());
        newOpportunity.setDescription(opportunity.description());
        newOpportunity.setRequirements(opportunity.requirements());
        newOpportunity.setBeginDate(opportunity.beginDate());
        newOpportunity.setEndDate(opportunity.endDate());
        newOpportunity.setCreatedBy(professor);

        ResearchOpportunity saved = repo.save(newOpportunity);
        return entityToDto(saved);
    }

    public ResearchOpportunityDto updateResearchOpportunity(UpdateResearchOpportunityDto updated, Long id) {
        ResearchOpportunity opportunity = repo.findById(id).orElseThrow(() -> new RuntimeException("There is no Research Opportunity with the id: " + id));

        //Validate Date
        if (updated.beginDate().isAfter(updated.endDate())) {
            throw new IllegalArgumentException("The beginning date must be before the end date");
        }
        if (updated.beginDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The beginning date must be after the today's date: " + LocalDate.now());
        }

        Optional.ofNullable(updated.title()).ifPresent(opportunity::setTitle);
        Optional.ofNullable(updated.description()).ifPresent(opportunity::setDescription);
        Optional.ofNullable(updated.requirements()).ifPresent(opportunity::setRequirements);
        Optional.ofNullable(updated.requirements()).ifPresent(opportunity::setRequirements);
        Optional.ofNullable(updated.beginDate()).ifPresent(opportunity::setBeginDate);
        Optional.ofNullable(updated.endDate()).ifPresent(opportunity::setEndDate);
        
        ResearchOpportunity saved = repo.save(opportunity);
        return entityToDto(saved);
    }

    public void deleteResearchOpportunity(Long id) {
        repo.deleteById(id);
    }

    // Entity to DTO Method 
    private ResearchOpportunityDto entityToDto(ResearchOpportunity opportunity) {
        return new ResearchOpportunityDto(
            opportunity.getId(),
            opportunity.getTitle(),
            opportunity.getDescription(),
            opportunity.getRequirements(),
            opportunity.getBeginDate(),
            opportunity.getEndDate(),
            opportunity.getCreatedAt(),
            opportunity.getUpdatedAt(),
            opportunity.getCreatedBy().getId(),
            opportunity.getCreatedBy().getName()
        );
    }
}
