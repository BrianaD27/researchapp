package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateResearchOpportunityDto;
import com.vsu.researchapp.application.dto.ResearchOpportunityDto;
import com.vsu.researchapp.application.dto.UpdateResearchOpportunityDto;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.queryObjects.OpportunitySearchCriteria;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;

@Service
public class ResearchOpportunityService {

    private final ResearchOpportunityRepositoryInterface repo;
    private final ProfessorRepositoryInterface professorRepo;

    public ResearchOpportunityService(ResearchOpportunityRepositoryInterface repo, ProfessorRepositoryInterface professorRepo) {
        this.repo = repo;
        this.professorRepo = professorRepo;
    }

    public List<ResearchOpportunityDto> getAllResearchOpportunities() {
        return repo.getAllResearchOpportunities().stream().map(this::entityToDto).toList();
    }

    public ResearchOpportunityDto getResearchOpportunityById(Long id) {
        return entityToDto(repo.getResearchOpportunity(id));
    }

    public List<ResearchOpportunityDto> getResearchOpportunitiesByProfessorId(Long professorId) {
        return repo.getResearchOpportunitiesByProfessorId(professorId).stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> getResearchOpportunitiesByUpcoming() {
        return repo.getResearchOpportunitiesByUpcoming().stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> getResearchOpportunitiesByDateRange(LocalDate earliestDate, LocalDate latestDate) {
        return repo.getResearchOpportunitiesByDateRange(earliestDate, latestDate).stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> getOpenForApplications() {
        return repo.getOpenForApplications().stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> search(String term) {
        return repo.searchOpportunities(term).stream().map(this::entityToDto).toList();
    }

    public List<ResearchOpportunityDto> searchByCriteria(OpportunitySearchCriteria criteria) {
        return repo.searchOpportunitiesByCriteria(criteria).stream().map(this::entityToDto).toList();
    }

    public ResearchOpportunityDto createResearchOpportunity(CreateResearchOpportunityDto dto, Long professorId) {
        Professor professor = professorRepo.getProfessorById(professorId);

        if (dto.startDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("The start date must be before the end date");
        }
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The start date must be after today's date: " + LocalDate.now());
        }

        ResearchOpportunity newOpportunity = new ResearchOpportunity();
        newOpportunity.setTitle(dto.title());
        newOpportunity.setDescription(dto.description());
        newOpportunity.setDepartment(dto.department());
        newOpportunity.setRequiredMajors(dto.requiredMajors());
        newOpportunity.setRequiredClassifications(dto.requiredClassifications());
        newOpportunity.setRequiredSkills(dto.requiredSkills());
        newOpportunity.setAvailability(dto.availability());
        newOpportunity.setMinimumGpa(dto.minimumGpa());
        newOpportunity.setApplicationDeadline(dto.applicationDeadline());
        newOpportunity.setStartDate(dto.startDate());
        newOpportunity.setEndDate(dto.endDate());
        newOpportunity.setResearchMediaUrls(dto.researchMediaUrls());
        newOpportunity.setProfessor(professor);

        return entityToDto(repo.createResearchOpportunity(newOpportunity));
    }

    public ResearchOpportunityDto updateResearchOpportunity(UpdateResearchOpportunityDto updated, Long id) {
        ResearchOpportunity opportunity = repo.getResearchOpportunity(id);

        LocalDate newStart = updated.startDate() != null ? updated.startDate() : opportunity.getStartDate();
        LocalDate newEnd = updated.endDate() != null ? updated.endDate() : opportunity.getEndDate();
        if (newStart != null && newEnd != null && newStart.isAfter(newEnd)) {
            throw new IllegalArgumentException("The start date must be before the end date");
        }

        Optional.ofNullable(updated.title()).ifPresent(opportunity::setTitle);
        Optional.ofNullable(updated.description()).ifPresent(opportunity::setDescription);
        Optional.ofNullable(updated.department()).ifPresent(opportunity::setDepartment);
        Optional.ofNullable(updated.requiredMajors()).ifPresent(opportunity::setRequiredMajors);
        Optional.ofNullable(updated.requiredClassifications()).ifPresent(opportunity::setRequiredClassifications);
        Optional.ofNullable(updated.requiredSkills()).ifPresent(opportunity::setRequiredSkills);
        Optional.ofNullable(updated.availability()).ifPresent(opportunity::setAvailability);
        Optional.ofNullable(updated.minimumGpa()).ifPresent(opportunity::setMinimumGpa);
        Optional.ofNullable(updated.applicationDeadline()).ifPresent(opportunity::setApplicationDeadline);
        Optional.ofNullable(updated.startDate()).ifPresent(opportunity::setStartDate);
        Optional.ofNullable(updated.endDate()).ifPresent(opportunity::setEndDate);
        Optional.ofNullable(updated.researchMediaUrls()).ifPresent(opportunity::setResearchMediaUrls);

        return entityToDto(repo.updateResearchOpportunity(opportunity));
    }

    public void deleteResearchOpportunity(Long id) {
        repo.deleteResearchOpportunity(id);
    }

    private ResearchOpportunityDto entityToDto(ResearchOpportunity opportunity) {
        return new ResearchOpportunityDto(
            opportunity.getId(),
            opportunity.getTitle(),
            opportunity.getDescription(),
            opportunity.getDepartment(),
            opportunity.getRequiredMajors(),
            opportunity.getRequiredClassifications(),
            opportunity.getRequiredSkills(),
            opportunity.getAvailability(),
            opportunity.getMinimumGpa(),
            opportunity.getApplicationDeadline(),
            opportunity.getStartDate(),
            opportunity.getEndDate(),
            opportunity.getResearchMediaUrls(),
            opportunity.getCreatedAt(),
            opportunity.getUpdatedAt(),
            opportunity.getProfessor().getId(),
            opportunity.getProfessor().getName()
        );
    }
}
