package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.ConductReport;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ConductReportRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Admin review of reported messages (/api/admin/** is ADMIN-only in SecurityConfig).
// - The reporter is told only that their report was reviewed, never the outcome.
// - The reported person is never told who reported them. A warning tells them
//   only that a message they sent was reported and reviewed.
@Service
public class ConductReportService {

    private static final Set<String> STATUSES = Set.of("NEW", "UNDER_REVIEW", "CLOSED");
    private static final Set<String> OUTCOMES = Set.of("NO_ACTION", "WARNING_SENT", "SUSPENDED");

    public record ConductReportDto(Long id, String status, String outcome, String reporterName,
                                   String reporterUsername, Long reportedAccountId, String reportedName,
                                   String reportedUsername, String reportedRole, boolean reportedActive,
                                   String messageBody, String context, String reason, String adminNotes,
                                   LocalDateTime warningSentAt, LocalDateTime closedAt, String closedBy,
                                   LocalDateTime createdAt) {}

    private final ConductReportRepository reports;
    private final UserAccountRepository accounts;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;

    public ConductReportService(ConductReportRepository reports, UserAccountRepository accounts,
            CurrentUserService currentUser, NotificationService notifications) {
        this.reports = reports;
        this.accounts = accounts;
        this.currentUser = currentUser;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<ConductReportDto> all() {
        requireAdmin();
        return reports.findTop300ByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public ConductReportDto update(Long id, String status, String outcome, String adminNotes) {
        UserAccount admin = requireAdmin();
        ConductReport r = find(id);
        String nextStatus = status == null ? r.getStatus() : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUSES.contains(nextStatus)) throw bad("Pick a status");
        String nextOutcome = outcome == null || outcome.isBlank() ? null : outcome.trim().toUpperCase(Locale.ROOT);
        if (nextOutcome != null && !OUTCOMES.contains(nextOutcome)) throw bad("Pick an outcome");
        if ("CLOSED".equals(nextStatus) && nextOutcome == null) throw bad("Pick an outcome before closing");
        String notes = adminNotes == null ? null : adminNotes.trim();
        if (notes != null && notes.length() > 2000) throw bad("Notes can be up to 2,000 characters");

        boolean closingNow = "CLOSED".equals(nextStatus) && !"CLOSED".equals(r.getStatus());
        r.setStatus(nextStatus);
        r.setOutcome("CLOSED".equals(nextStatus) ? nextOutcome : r.getOutcome());
        r.setAdminNotes(notes == null || notes.isEmpty() ? null : notes);
        if (closingNow) {
            r.setClosedAt(LocalDateTime.now());
            r.setClosedBy(admin.getUsername());
        } else if (!"CLOSED".equals(nextStatus)) {
            r.setClosedAt(null);
            r.setClosedBy(null);
        }
        reports.save(r);

        if (closingNow) {
            notifications.notify(r.getReporterAccountId(), "CONDUCT_REPORT", "Your report was reviewed",
                "Your report has been reviewed. Thank you for helping keep the VSU Research Network safe.", "/messages");
        }
        return toDto(r);
    }

    @Transactional
    public ConductReportDto sendWarning(Long id) {
        requireAdmin();
        ConductReport r = find(id);
        notifications.notify(r.getReportedAccountId(), "CONDUCT_WARNING", "A message you sent was reported",
            "A message you sent in the VSU Research Network was reported and reviewed by an admin. "
                + "Please keep messages respectful and follow VSU's conduct expectations.", "/messages");
        r.setWarningSentAt(LocalDateTime.now());
        if (!"CLOSED".equals(r.getStatus())) r.setStatus("UNDER_REVIEW");
        reports.save(r);
        return toDto(r);
    }

    // Turns off the reported account. JwtFilter checks "active" on every request,
    // so they are signed out right away. Admin accounts can't be suspended here.
    @Transactional
    public ConductReportDto suspend(Long id) {
        UserAccount admin = requireAdmin();
        ConductReport r = find(id);
        UserAccount target = accounts.findById(r.getReportedAccountId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if ("ADMIN".equalsIgnoreCase(target.getRole()) || target.getId().equals(admin.getId())) {
            throw bad("Admin accounts can't be suspended from here");
        }
        target.setActive(false);
        accounts.save(target);
        if (!"CLOSED".equals(r.getStatus())) r.setStatus("UNDER_REVIEW");
        reports.save(r);
        return toDto(r);
    }

    private ConductReport find(Long id) {
        return reports.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    private UserAccount requireAdmin() {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (!currentUser.isAdmin(me)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        return me;
    }

    private ConductReportDto toDto(ConductReport r) {
        UserAccount reporter = accounts.findById(r.getReporterAccountId()).orElse(null);
        UserAccount reported = accounts.findById(r.getReportedAccountId()).orElse(null);
        return new ConductReportDto(r.getId(), r.getStatus(), r.getOutcome(),
            reporter == null ? "Deleted account" : reporter.getUsername(),
            reporter == null ? null : reporter.getUsername(),
            r.getReportedAccountId(),
            reported == null ? "Deleted account" : reported.getUsername(),
            reported == null ? null : reported.getUsername(),
            reported == null ? null : reported.getRole(),
            reported != null && reported.isActive(),
            r.getMessageBody(), r.getContext(), r.getReason(), r.getAdminNotes(),
            r.getWarningSentAt(), r.getClosedAt(), r.getClosedBy(), r.getCreatedAt());
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
