package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.RemovalRequest;

public interface RemovalRequestRepository extends JpaRepository<RemovalRequest, Long> {

    List<RemovalRequest> findByProfessorIdAndStatusOrderByCreatedAtDesc(Long professorId, String status);

    boolean existsByOpportunityIdAndStatus(Long opportunityId, String status);
}
