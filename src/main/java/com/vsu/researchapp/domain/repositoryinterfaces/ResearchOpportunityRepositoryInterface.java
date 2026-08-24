package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDate;
import java.util.List;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.queryObjects.OpportunitySearchCriteria;

public interface ResearchOpportunityRepositoryInterface {
    // CRUD Operations
    ResearchOpportunity createResearchOpportunity(ResearchOpportunity opportunity);
    ResearchOpportunity getResearchOpportunity(Long id);
    ResearchOpportunity updateResearchOpportunity(ResearchOpportunity opportunity);
    void deleteResearchOpportunity(Long id);
    List<ResearchOpportunity> getAllResearchOpportunities();

    // Lookups
    List<ResearchOpportunity> getResearchOpportunitiesByProfessorId(Long professorId);
    List<ResearchOpportunity> getResearchOpportunitiesByDepartment(String department);
    List<ResearchOpportunity> getResearchOpportunitiesByUpcoming();
    List<ResearchOpportunity> getResearchOpportunitiesByDateRange(LocalDate earliestDate, LocalDate latestDate);
    List<ResearchOpportunity> getOpenForApplications();

    // Search
    List<ResearchOpportunity> searchOpportunities(String term);
    List<ResearchOpportunity> searchOpportunitiesByCriteria(OpportunitySearchCriteria criteria);
}
