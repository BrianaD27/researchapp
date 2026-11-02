package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.MessageBlock;

public interface MessageBlockRepository extends JpaRepository<MessageBlock, Long> {

    boolean existsByBlockerAccountIdAndBlockedAccountId(Long blockerAccountId, Long blockedAccountId);

    Optional<MessageBlock> findByBlockerAccountIdAndBlockedAccountId(Long blockerAccountId, Long blockedAccountId);

    List<MessageBlock> findByBlockerAccountId(Long blockerAccountId);

    List<MessageBlock> findByBlockedAccountId(Long blockedAccountId);
}
