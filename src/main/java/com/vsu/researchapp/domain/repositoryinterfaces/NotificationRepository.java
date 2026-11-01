package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.vsu.researchapp.domain.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop30ByRecipientUserAccountIdOrderByCreatedAtDesc(Long recipientUserAccountId);

    long countByRecipientUserAccountIdAndReadFalse(Long recipientUserAccountId);

    Optional<Notification> findByIdAndRecipientUserAccountId(Long id, Long recipientUserAccountId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipientUserAccountId = :accountId AND n.read = false")
    int markAllRead(@Param("accountId") Long accountId);
}
