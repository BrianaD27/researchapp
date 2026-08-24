package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.model.SavedOpportunity;
import com.vsu.researchapp.domain.repositoryinterfaces.SavedOpportunityRepositoryInterface;

@Repository
public class SavedOpportunityRepositoryImpl implements SavedOpportunityRepositoryInterface {

    private final SavedOpportunityRepository savedOpportunityRepository;

    public SavedOpportunityRepositoryImpl(SavedOpportunityRepository savedOpportunityRepository) {
        this.savedOpportunityRepository = savedOpportunityRepository;
    }

    @Override
    public SavedOpportunity saveOpportunity(SavedOpportunity savedOpportunity) {
        return savedOpportunityRepository.save(savedOpportunity);
    }

    @Override
    public SavedOpportunity getSavedOpportunityById(Long id) {
        return savedOpportunityRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("There is no saved opportunity with the id: " + id));
    }

    @Override
    public void unsaveOpportunityByStudentIdAndOpportunityId(Long studentId, Long opportunityId) {
        savedOpportunityRepository.deleteByStudentIdAndOpportunityId(studentId, opportunityId);
    }

    @Override
    public List<SavedOpportunity> getSavedOpportunitiesByStudentId(Long studentId) {
        return savedOpportunityRepository.findByStudentId(studentId);
    }

    @Override
    public boolean isOpportunitySavedByStudent(Long studentId, Long opportunityId) {
        return savedOpportunityRepository.existsByStudentIdAndOpportunityId(studentId, opportunityId);
    }

    @Override
    public Optional<SavedOpportunity> getSavedOpportunityByStudentIdAndOpportunityId(Long studentId, Long opportunityId) {
        return savedOpportunityRepository.findByStudentIdAndOpportunityId(studentId, opportunityId);
    }
}
