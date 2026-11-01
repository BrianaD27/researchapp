package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.AssociateLink;

public interface AssociateLinkRepository extends JpaRepository<AssociateLink, Long> {

    List<AssociateLink> findByAssociateUserAccountIdOrderByIdAsc(Long associateUserAccountId);

    List<AssociateLink> findByProfessorIdOrderByIdAsc(Long professorId);

    Optional<AssociateLink> findByProfessorIdAndAssociateUserAccountId(Long professorId, Long associateUserAccountId);

    long countByAssociateUserAccountId(Long associateUserAccountId);

    boolean existsByAssociateUserAccountIdAndCanReviewTrue(Long associateUserAccountId);
}
