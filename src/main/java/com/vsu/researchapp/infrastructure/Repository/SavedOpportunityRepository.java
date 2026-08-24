package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.SavedOpportunity;

public interface SavedOpportunityRepository extends JpaRepository<SavedOpportunity, Long> {

    List<SavedOpportunity> findByStudentId(Long studentId);

    Optional<SavedOpportunity> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId);

    boolean existsByStudentIdAndOpportunityId(Long studentId, Long opportunityId);

    void deleteByStudentIdAndOpportunityId(Long studentId, Long opportunityId);
}
