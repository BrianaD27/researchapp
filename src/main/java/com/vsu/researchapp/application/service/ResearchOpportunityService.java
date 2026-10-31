package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

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
    private final CurrentUserService currentUserService;

    public ResearchOpportunityService(ResearchOpportunityRepositoryInterface repo, ProfessorRepositoryInterface professorRepo, CurrentUserService currentUserService) {
        this.repo = repo;
        this.professorRepo = professorRepo;
        this.currentUserService = currentUserService;
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

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
    public ResearchOpportunityDto createResearchOpportunity(CreateResearchOpportunityDto dto, Long professorId) {
        Professor professor = professorRepo.getProfessorById(professorId);
        UserAccount account = currentUserService.getCurrentUserAccount();
        if (!currentUserService.isAdmin(account)
                && (professor.getUserAccountId() == null
                    || !professor.getUserAccountId().equals(account.getId()))) {
            throw new AccessDeniedException(
                "You can only create opportunities for your own faculty profile");
        }

        if (dto.startDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("The start date must be before the end date");
        }
        if (dto.startDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The start date must be after today's date: " + LocalDate.now());
        }

        if (dto.applicationOpenDate() != null
                && dto.applicationOpenDate().isAfter(dto.applicationDeadline())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Applications must open on or before they close");
        }
        if (dto.applicationDeadline().isAfter(dto.startDate())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Applications must close on or before the project starts");
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
        newOpportunity.setApplicationOpenDate(dto.applicationOpenDate());
        newOpportunity.setStartDate(dto.startDate());
        newOpportunity.setEndDate(dto.endDate());
        newOpportunity.setResearchMediaUrls(dto.researchMediaUrls());
        newOpportunity.setProfessor(professor);

        return entityToDto(repo.createResearchOpportunity(newOpportunity));
    }

    @PreAuthorize("@studentOpportunityAccess.opportunity(#p1)")
    public ResearchOpportunityDto updateResearchOpportunity(UpdateResearchOpportunityDto updated, Long id) {
        ResearchOpportunity opportunity = repo.getResearchOpportunity(id);

        LocalDate newStart = updated.startDate() != null ? updated.startDate() : opportunity.getStartDate();
        LocalDate newEnd = updated.endDate() != null ? updated.endDate() : opportunity.getEndDate();
        if (newStart != null && newEnd != null && newStart.isAfter(newEnd)) {
            throw new IllegalArgumentException("The start date must be before the end date");
        }

        LocalDate opens = updated.applicationOpenDate() != null
            ? updated.applicationOpenDate() : opportunity.getApplicationOpenDate();
        LocalDate closes = updated.applicationDeadline() != null
            ? updated.applicationDeadline() : opportunity.getApplicationDeadline();
        if (opens != null && closes != null && opens.isAfter(closes)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Applications must open on or before they close");
        }
        if (closes != null && newStart != null && closes.isAfter(newStart)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST,
                "Applications must close on or before the project starts");
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
        Optional.ofNullable(updated.applicationOpenDate()).ifPresent(opportunity::setApplicationOpenDate);
        Optional.ofNullable(updated.startDate()).ifPresent(opportunity::setStartDate);
        Optional.ofNullable(updated.endDate()).ifPresent(opportunity::setEndDate);
        Optional.ofNullable(updated.researchMediaUrls()).ifPresent(opportunity::setResearchMediaUrls);

        return entityToDto(repo.updateResearchOpportunity(opportunity, id));
    }

    @PreAuthorize("@studentOpportunityAccess.opportunity(#p0)")
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
            opportunity.getApplicationOpenDate(),
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
