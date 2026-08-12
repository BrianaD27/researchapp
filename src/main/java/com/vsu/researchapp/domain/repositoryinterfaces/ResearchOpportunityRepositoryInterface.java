package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.queryObjects.OpportunitySearchCriteria;

public interface ResearchOpportunityRepositoryInterface {
    ResearchOpportunity createResearchOpportunity(ResearchOpportunity opportunity);
    ResearchOpportunity getResearchOpportunity(Long id);
    ResearchOpportunity updateResearchOpportunity(ResearchOpportunity opportunity);
    void deleteResearchOpportunity(Long id);
    List<ResearchOpportunity> getAllResearchOpportunities();
    List<ResearchOpportunity> searchOpportunitiesByCriteria(OpportunitySearchCriteria criteria);

}
