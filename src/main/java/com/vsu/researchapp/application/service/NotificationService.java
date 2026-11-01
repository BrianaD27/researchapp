package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.NotificationDto;
import com.vsu.researchapp.domain.model.Notification;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.NotificationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.externalServices.email.emailService;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notifications;
    private final UserAccountRepository accounts;
    private final CurrentUserService currentUser;
    private final emailService email;

    public NotificationService(NotificationRepository notifications, UserAccountRepository accounts,
            CurrentUserService currentUser, emailService email) {
        this.notifications = notifications;
        this.accounts = accounts;
        this.currentUser = currentUser;
        this.email = email;
    }

    // Saves an in-app alert and emails it. Never throws: a failed alert must not
    // undo the action that triggered it (an application, a decision, and so on).
    public void notify(Long recipientUserAccountId, String type, String subject, String message, String link) {
        if (recipientUserAccountId == null) return;
        try {
            Notification notification = new Notification();
            notification.setRecipientUserAccountId(recipientUserAccountId);
            notification.setType(type);
            notification.setMessage(message.length() > 500 ? message.substring(0, 500) : message);
            notification.setLink(link != null && link.startsWith("/") ? link : null);
            notifications.save(notification);

            String address = accounts.findById(recipientUserAccountId).map(UserAccount::getEmail).orElse(null);
            if (address != null && !address.isBlank()) {
                CompletableFuture.runAsync(() -> email.sendNotification(address, subject, message, link));
            }
        } catch (Exception e) {
            logger.warn("[NOTIFY] Could not record notification: {}", e.getMessage());
        }
    }

    public List<NotificationDto> mine() {
        Long me = currentUser.getCurrentUserAccount().getId();
        return notifications.findTop30ByRecipientUserAccountIdOrderByCreatedAtDesc(me).stream()
            .map(n -> new NotificationDto(n.getId(), n.getType(), n.getMessage(), n.getLink(), n.isRead(), n.getCreatedAt()))
            .toList();
    }

    public long unreadCount() {
        return notifications.countByRecipientUserAccountIdAndReadFalse(currentUser.getCurrentUserAccount().getId());
    }

    // Only the recipient can mark their own alert read; anyone else gets "not found".
    public void markRead(Long id) {
        Long me = currentUser.getCurrentUserAccount().getId();
        Notification notification = notifications.findByIdAndRecipientUserAccountId(id, me)
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        notifications.save(notification);
    }

    public void markAllRead() {
        notifications.markAllRead(currentUser.getCurrentUserAccount().getId());
    }
}
