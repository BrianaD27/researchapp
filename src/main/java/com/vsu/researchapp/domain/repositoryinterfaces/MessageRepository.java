package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop200ByConversationIdOrderByCreatedAtDesc(Long conversationId);

    long countByCreatedAtAfter(LocalDateTime after);

    long countByConversationIdAndSenderSideNot(Long conversationId, String senderSide);

    long countByConversationIdAndSenderSideNotAndCreatedAtAfter(Long conversationId, String senderSide, LocalDateTime after);
}
