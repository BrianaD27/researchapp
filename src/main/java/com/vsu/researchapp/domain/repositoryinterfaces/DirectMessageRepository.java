package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.DirectMessage;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    List<DirectMessage> findTop200ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    List<DirectMessage> findTop6ByConversationIdAndIdLessThanEqualOrderByIdDesc(Long conversationId, Long id);

    long countByCreatedAtAfter(LocalDateTime after);

    long countBySenderAccountIdAndCreatedAtAfter(Long senderAccountId, LocalDateTime after);

    long countByConversationIdAndSenderAccountIdNot(Long conversationId, Long senderAccountId);

    long countByConversationIdAndSenderAccountIdNotAndCreatedAtAfter(Long conversationId, Long senderAccountId,
        LocalDateTime after);
}
