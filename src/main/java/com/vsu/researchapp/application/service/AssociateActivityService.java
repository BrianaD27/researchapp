package com.vsu.researchapp.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.model.AssociateActivity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateActivityRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Writes the associate audit trail. Records only when the person acting is a
// Research Associate, so professors' own actions don't clutter the list.
@Service
public class AssociateActivityService {

    private static final Logger logger = LoggerFactory.getLogger(AssociateActivityService.class);

    private final AssociateActivityRepository activity;
    private final CurrentUserService currentUser;

    public AssociateActivityService(AssociateActivityRepository activity, CurrentUserService currentUser) {
        this.activity = activity;
        this.currentUser = currentUser;
    }

    public void record(Long professorId, String action, String detail) {
        if (professorId == null) return;
        try {
            UserAccount user = currentUser.getCurrentUserAccount();
            if (!AssociateService.ROLE.equalsIgnoreCase(user.getRole())) return;
            AssociateActivity entry = new AssociateActivity();
            entry.setProfessorId(professorId);
            entry.setActorUsername(user.getUsername());
            entry.setAction(action);
            entry.setDetail(detail != null && detail.length() > 500 ? detail.substring(0, 500) : detail);
            activity.save(entry);
        } catch (Exception e) {
            logger.warn("[ASSOCIATE AUDIT] Could not record activity: {}", e.getMessage());
        }
    }
}
