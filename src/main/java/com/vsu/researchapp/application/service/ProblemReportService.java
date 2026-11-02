package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.application.dto.ProblemReportDto;
import com.vsu.researchapp.domain.model.ProblemReport;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ProblemReportRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// "Report a Problem": any signed-in user can file a report; every active admin
// gets a bell alert and an email right away. Only admins can see all reports
// and change their status; reporters see only their own.
@Service
public class ProblemReportService {

    public static final Set<String> CATEGORIES =
        Set.of("Something is broken", "Can't find something", "Account or login", "Suggestion", "Other",
            "Inappropriate message");
    private static final Set<String> STATUSES = Set.of("NEW", "IN_PROGRESS", "FIXED");
    private static final int MAX_PER_HOUR = 5;

    private final ProblemReportRepository reports;
    private final UserAccountRepository accounts;
    private final CurrentUserService currentUser;
    private final NotificationService notifications;

    public ProblemReportService(ProblemReportRepository reports, UserAccountRepository accounts,
            CurrentUserService currentUser, NotificationService notifications) {
        this.reports = reports;
        this.accounts = accounts;
        this.currentUser = currentUser;
        this.notifications = notifications;
    }

    public ProblemReportDto submit(String category, String description, String page, String browser) {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (category == null || !CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pick a category");
        }
        String text = description == null ? "" : description.trim();
        if (text.length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Describe the problem in at least 10 characters");
        }
        if (text.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Keep the description under 2,000 characters");
        }
        if (reports.countByReporterUserAccountIdAndCreatedAtAfter(me.getId(), LocalDateTime.now().minusHours(1)) >= MAX_PER_HOUR) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "You have sent several reports in the last hour. Please wait before sending another");
        }

        ProblemReport report = new ProblemReport();
        report.setReporterUserAccountId(me.getId());
        report.setReporterUsername(me.getUsername());
        report.setCategory(category);
        report.setDescription(text);
        report.setPage(page != null && page.startsWith("/") ? clip(page, 255) : null);
        report.setBrowser(browser == null ? null : clip(browser, 255));
        report = reports.save(report);

        String alert = "New problem report from " + me.getUsername() + " (" + category + "): " + clip(text, 200);
        for (UserAccount admin : accounts.findAll()) {
            if (currentUser.isAdmin(admin) && admin.isActive()) {
                notifications.notify(admin.getId(), "PROBLEM_REPORT", "New problem report", alert, "/admin-reports");
            }
        }
        return toDto(report);
    }

    public List<ProblemReportDto> mine() {
        Long me = currentUser.getCurrentUserAccount().getId();
        return reports.findTop50ByReporterUserAccountIdOrderByCreatedAtDesc(me).stream().map(this::toDto).toList();
    }

    public List<ProblemReportDto> all() {
        requireAdmin();
        return reports.findTop200ByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    public ProblemReportDto update(Long id, String status, String reply) {
        requireAdmin();
        if (status == null || !STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be NEW, IN_PROGRESS, or FIXED");
        }
        ProblemReport report = reports.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found"));
        String cleanReply = reply == null || reply.isBlank() ? null : clip(reply.trim(), 2000);
        boolean changed = !status.equals(report.getStatus())
            || (cleanReply != null && !cleanReply.equals(report.getAdminReply()));
        report.setStatus(status);
        report.setAdminReply(cleanReply);
        report = reports.save(report);

        if (changed) {
            String label = switch (status) {
                case "IN_PROGRESS" -> "in progress";
                case "FIXED" -> "fixed";
                default -> "received";
            };
            String message = "Your problem report is " + label + "."
                + (cleanReply != null ? " Admin reply: " + clip(cleanReply, 300) : "");
            notifications.notify(report.getReporterUserAccountId(), "PROBLEM_REPORT_UPDATE",
                "Update on your problem report", message, "/report-problem");
        }
        return toDto(report);
    }

    private void requireAdmin() {
        if (!currentUser.isAdmin(currentUser.getCurrentUserAccount())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can manage problem reports");
        }
    }

    private ProblemReportDto toDto(ProblemReport r) {
        return new ProblemReportDto(r.getId(), r.getCategory(), r.getDescription(), r.getPage(), r.getBrowser(),
            r.getReporterUsername(), r.getStatus(), r.getAdminReply(), r.getCreatedAt(), r.getUpdatedAt());
    }

    private static String clip(String value, int max) {
        return value.length() > max ? value.substring(0, max) : value;
    }
}
