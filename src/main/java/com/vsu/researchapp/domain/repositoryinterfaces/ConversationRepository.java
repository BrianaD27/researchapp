package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    List<Conversation> findByStudentUserAccountIdOrderByLastMessageAtDesc(Long studentUserAccountId);

    List<Conversation> findByProfessorIdOrderByLastMessageAtDesc(Long professorId);

    Optional<Conversation> findByStudentUserAccountIdAndProfessorId(Long studentUserAccountId, Long professorId);

    List<Conversation> findByLastMessageAtAfter(LocalDateTime after);
}
