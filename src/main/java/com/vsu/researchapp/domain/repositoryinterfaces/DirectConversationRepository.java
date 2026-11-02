package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.DirectConversation;

public interface DirectConversationRepository extends JpaRepository<DirectConversation, Long> {

    List<DirectConversation> findByUserLowIdOrUserHighId(Long userLowId, Long userHighId);

    Optional<DirectConversation> findByUserLowIdAndUserHighId(Long userLowId, Long userHighId);

    List<DirectConversation> findByLastMessageAtAfter(LocalDateTime after);

    long countByCreatedByAndCreatedAtAfter(Long createdBy, LocalDateTime after);
}
