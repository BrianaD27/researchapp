package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.RemovalRequest;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateActivityRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.RemovalRequestRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// Research Associates: who works for which professor, with what permissions,
// plus removal requests that need the professor's approval.
@Service
public class AssociateService {

    public static final String ROLE = "ASSOCIATE";
    public static final int MAX_PROFESSORS_PER_ASSOCIATE = 7;

    public record WorkingFor(Long linkId, Long professorId, String professorName, boolean canPublish, boolean canReview) {}
    public record SessionInfo(String username, String role, List<WorkingFor> workingFor) {}
    public record AssociateView(Long linkId, Long userAccountId, String username, String email, boolean canPublish, boolean canReview) {}
    public record RemovalView(Long id, Long opportunityId, String opportunityTitle, String requestedBy, String reason,
                              String status, String outcome, LocalDateTime createdAt) {}
    public record ActivityView(String actor, String action, String detail, LocalDateTime createdAt) {}
    public record TeamOverview(Long professorId, String professorName, List<AssociateView> associates,
                               List<RemovalView> pendingRemovals, List<ActivityView> activity) {}

    private final AssociateLinkRepository links;
    private final RemovalRequestRepository removals;
    private final AssociateActivityRepository activityLog;
    private final AssociateActivityService activity;
    private final UserAccountRepository accounts;
    private final ProfessorRepositoryInterface professors;
    private final ResearchOpportunityRepositoryInterface opportunities;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;

    @PersistenceContext
    private EntityManager entityManager;

    public AssociateService(AssociateLinkRepository links, RemovalRequestRepository removals,
            AssociateActivityRepository activityLog, AssociateActivityService activity,
            UserAccountRepository accounts, ProfessorRepositoryInterface professors,
            ResearchOpportunityRepositoryInterface opportunities, CurrentUserService currentUser,
            NotificationService notifications) {
        this.links = links;
        this.removals = removals;
        this.activityLog = activityLog;
        this.activity = activity;
        this.accounts = accounts;
        this.professors = professors;
        this.opportunities = opportunities;
        this.currentUser = currentUser;
        this.notifications = notifications;
    }

    // Any logged-in user: who am I, and (for associates) who do I work for.
    @Transactional(readOnly = true)
    public SessionInfo session() {
        UserAccount user = currentUser.getCurrentUserAccount();
        List<WorkingFor> workingFor = ROLE.equalsIgnoreCase(user.getRole())
            ? links.findByAssociateUserAccountIdOrderByIdAsc(user.getId()).stream()
                .map(l -> new WorkingFor(l.getId(), l.getProfessorId(), professorName(l.getProfessorId()),
                    l.isCanPublish(), l.isCanReview()))
                .toList()
            : List.of();
        return new SessionInfo(user.getUsername(), user.getRole(), workingFor);
    }

    @Transactional(readOnly = true)
    public TeamOverview team(Long professorId) {
        requireOwnerOrAdmin(professorId);
        return buildTeam(professorId);
    }

    @Transactional
    public TeamOverview add(Long professorId, String username, boolean canPublish, boolean canReview) {
        requireOwnerOrAdmin(professorId);
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Enter the associate's username.");
        }
        if (!canPublish && !canReview) {
            throw new IllegalArgumentException("Give the associate at least one permission.");
        }
        UserAccount account = accounts.findByUsername(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("No account has that username."));
        if (!ROLE.equalsIgnoreCase(account.getRole())) {
            throw new IllegalArgumentException(
                "That account is not a Research Associate yet. An admin can change its role on the Users page.");
        }
        if (links.findByProfessorIdAndAssociateUserAccountId(professorId, account.getId()).isPresent()) {
            throw new IllegalArgumentException("That associate is already on your team.");
        }
        if (links.countByAssociateUserAccountId(account.getId()) >= MAX_PROFESSORS_PER_ASSOCIATE) {
            throw new IllegalArgumentException("That associate already works for " + MAX_PROFESSORS_PER_ASSOCIATE
                + " professors, the most allowed.");
        }
        AssociateLink link = new AssociateLink();
        link.setProfessorId(professorId);
        link.setAssociateUserAccountId(account.getId());
        link.setCanPublish(canPublish);
        link.setCanReview(canReview);
        links.save(link);
        notifications.notify(account.getId(), "ASSOCIATE_ADDED", "Added as a Research Associate",
            "You were added as a Research Associate for " + professorName(professorId)
                + ". Use \"Working for\" at the top of the page to switch between professors.",
            "/faculty-view-all");
        return buildTeam(professorId);
    }

    @Transactional
    public TeamOverview updatePermissions(Long linkId, boolean canPublish, boolean canReview) {
        AssociateLink link = findLink(linkId);
        requireOwnerOrAdmin(link.getProfessorId());
        if (!canPublish && !canReview) {
            throw new IllegalArgumentException("Give the associate at least one permission, or remove them.");
        }
        link.setCanPublish(canPublish);
        link.setCanReview(canReview);
        links.save(link);
        return buildTeam(link.getProfessorId());
    }

    @Transactional
    public TeamOverview removeLink(Long linkId) {
        AssociateLink link = findLink(linkId);
        requireOwnerOrAdmin(link.getProfessorId());
        Long professorId = link.getProfessorId();
        links.delete(link);
        notifications.notify(link.getAssociateUserAccountId(), "ASSOCIATE_REMOVED", "Research Associate update",
            "You are no longer a Research Associate for " + professorName(professorId) + ".", "/faculty-view-all");
        return buildTeam(professorId);
    }

    @Transactional
    public RemovalView requestRemoval(Long opportunityId, String reason) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (!ROLE.equalsIgnoreCase(user.getRole())) {
            throw new AccessDeniedException("Only Research Associates send removal requests.");
        }
        ResearchOpportunity opportunity = opportunities.getResearchOpportunity(opportunityId);
        Professor professor = opportunity.getProfessor();
        if (professor == null) {
            throw new IllegalArgumentException("This project has no faculty owner.");
        }
        links.findByProfessorIdAndAssociateUserAccountId(professor.getId(), user.getId())
            .filter(AssociateLink::isCanPublish)
            .orElseThrow(() -> new AccessDeniedException(
                "You can only request removal for professors you publish for."));
        if (removals.existsByOpportunityIdAndStatus(opportunityId, RemovalRequest.PENDING)) {
            throw new IllegalArgumentException("A removal request for this project is already waiting for approval.");
        }
        String cleanReason = reason == null ? "" : reason.trim();
        if (cleanReason.length() > 500) {
            throw new IllegalArgumentException("Keep the reason under 500 characters.");
        }
        RemovalRequest request = new RemovalRequest();
        request.setOpportunityId(opportunityId);
        request.setOpportunityTitle(opportunity.getTitle());
        request.setProfessorId(professor.getId());
        request.setRequestedByUserAccountId(user.getId());
        request.setRequestedByUsername(user.getUsername());
        request.setReason(cleanReason.isEmpty() ? null : cleanReason);
        removals.save(request);
        activity.record(professor.getId(), "REQUESTED_REMOVAL",
            "\"" + opportunity.getTitle() + "\"" + (cleanReason.isEmpty() ? "" : ": " + cleanReason));
        notifications.notify(professor.getUserAccountId(), "REMOVAL_REQUESTED", "Removal request",
            user.getUsername() + " asked to remove \"" + opportunity.getTitle()
                + "\". Approve or decline it on your Research Associates page.",
            "/faculty-associates");
        return toView(request);
    }

    @Transactional
    public TeamOverview decideRemoval(Long requestId, boolean approve) {
        RemovalRequest request = removals.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("That removal request no longer exists."));
        requireOwnerOrAdmin(request.getProfessorId());
        if (!RemovalRequest.PENDING.equals(request.getStatus())) {
            throw new IllegalArgumentException("That removal request was already decided.");
        }
        String outcome = approve ? takeDown(request.getOpportunityId()) : "Kept";
        request.setStatus(approve ? RemovalRequest.APPROVED : RemovalRequest.DENIED);
        request.setOutcome(outcome);
        request.setDecidedAt(LocalDateTime.now());
        removals.save(request);
        String title = "\"" + request.getOpportunityTitle() + "\"";
        notifications.notify(request.getRequestedByUserAccountId(), "REMOVAL_DECIDED",
            approve ? "Removal approved" : "Removal declined",
            approve ? "Your request to remove " + title + " was approved. " + outcome + "."
                    : "Your request to remove " + title + " was declined. The project stays up.",
            "/faculty-view-all");
        return buildTeam(request.getProfessorId());
    }

    // Deletes the project when nothing points at it; otherwise closes it so student
    // applications and saved lists stay intact.
    private String takeDown(Long opportunityId) {
        ResearchOpportunity opportunity;
        try {
            opportunity = opportunities.getResearchOpportunity(opportunityId);
        } catch (RuntimeException e) {
            return "It was already removed";
        }
        Long applications = entityManager.createQuery(
                "select count(a) from Application a where a.researchOpportunity.id = :id", Long.class)
            .setParameter("id", opportunityId).getSingleResult();
        Long saves = entityManager.createQuery(
                "select count(s) from SavedOpportunity s where s.opportunity.id = :id", Long.class)
            .setParameter("id", opportunityId).getSingleResult();
        if (applications == 0 && saves == 0) {
            opportunities.deleteResearchOpportunity(opportunityId);
            return "The project was deleted";
        }
        opportunity.setAvailability("Closed");
        opportunities.updateResearchOpportunity(opportunity, opportunityId);
        return "The project was closed instead of deleted, because students have applied to or saved it";
    }

    private TeamOverview buildTeam(Long professorId) {
        List<AssociateView> associates = links.findByProfessorIdOrderByIdAsc(professorId).stream()
            .map(l -> {
                UserAccount account = accounts.findById(l.getAssociateUserAccountId()).orElse(null);
                return new AssociateView(l.getId(), l.getAssociateUserAccountId(),
                    account == null ? "(deleted account)" : account.getUsername(),
                    account == null ? null : account.getEmail(),
                    l.isCanPublish(), l.isCanReview());
            })
            .toList();
        List<RemovalView> pending = removals
            .findByProfessorIdAndStatusOrderByCreatedAtDesc(professorId, RemovalRequest.PENDING).stream()
            .map(this::toView)
            .toList();
        List<ActivityView> recent = activityLog.findTop50ByProfessorIdOrderByCreatedAtDesc(professorId).stream()
            .map(a -> new ActivityView(a.getActorUsername(), a.getAction(), a.getDetail(), a.getCreatedAt()))
            .toList();
        return new TeamOverview(professorId, professorName(professorId), associates, pending, recent);
    }

    private RemovalView toView(RemovalRequest r) {
        return new RemovalView(r.getId(), r.getOpportunityId(), r.getOpportunityTitle(), r.getRequestedByUsername(),
            r.getReason(), r.getStatus(), r.getOutcome(), r.getCreatedAt());
    }

    private AssociateLink findLink(Long linkId) {
        return links.findById(linkId)
            .orElseThrow(() -> new IllegalArgumentException("That associate is no longer on the team."));
    }

    private void requireOwnerOrAdmin(Long professorId) {
        UserAccount user = currentUser.getCurrentUserAccount();
        if (currentUser.isAdmin(user)) return;
        Professor professor = professors.getProfessorById(professorId);
        if (professor.getUserAccountId() == null || !professor.getUserAccountId().equals(user.getId())) {
            throw new AccessDeniedException("Only this professor can manage their Research Associates.");
        }
    }

    private String professorName(Long professorId) {
        try {
            return professors.getProfessorById(professorId).getName();
        } catch (RuntimeException e) {
            return "Faculty #" + professorId;
        }
    }
}
