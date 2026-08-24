package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.SavedOpportunity;

public interface SavedOpportunityRepositoryInterface {
    SavedOpportunity saveOpportunity(SavedOpportunity savedOpportunity);
    SavedOpportunity getSavedOpportunityById(Long id);
    void unsaveOpportunityByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
    List<SavedOpportunity> getSavedOpportunitiesByStudentId(Long studentId);

    // Prevent bookmarking the same opportunity twice
    boolean isOpportunitySavedByStudent(Long studentId, Long opportunityId);
    Optional<SavedOpportunity> getSavedOpportunityByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
}
