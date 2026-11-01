package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.ProblemReport;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ConversationRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.MessageRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ProblemReportRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ApplicationJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ResearchOpportunityJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.StudentJpaRepository;

// Numbers for the admin dashboard. Counts only: no names, no message text.
@Service
public class AdminStatsService {

    public record Stats(
        LocalDateTime generatedAt,
        Map<String, Object> users,
        Map<String, Long> usersByRole,
        Map<String, Object> profiles,
        Map<String, Object> research,
        Map<String, Object> applications,
        Map<String, Object> messages,
        Map<String, Object> problemReports
    ) {}

    private final UserAccountRepository accounts;
    private final StudentJpaRepository students;
    private final ProfessorJpaRepository professors;
    private final ResearchOpportunityJpaRepository opportunities;
    private final ApplicationJpaRepository applications;
    private final ConversationRepository conversations;
    private final MessageRepository messages;
    private final ProblemReportRepository reports;

    public AdminStatsService(UserAccountRepository accounts, StudentJpaRepository students,
            ProfessorJpaRepository professors, ResearchOpportunityJpaRepository opportunities,
            ApplicationJpaRepository applications, ConversationRepository conversations,
            MessageRepository messages, ProblemReportRepository reports) {
        this.accounts = accounts;
        this.students = students;
        this.professors = professors;
        this.opportunities = opportunities;
        this.applications = applications;
        this.conversations = conversations;
        this.messages = messages;
        this.reports = reports;
    }

    @Transactional(readOnly = true)
    public Stats stats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDate today = LocalDate.now();

        List<UserAccount> users = accounts.findAll();
        Map<String, Long> byRole = new TreeMap<>();
        for (UserAccount u : users) {
            String role = u.getRole() == null ? "NONE" : u.getRole().toUpperCase();
            byRole.merge(role, 1L, Long::sum);
        }
        Map<String, Object> userStats = new LinkedHashMap<>();
        userStats.put("total", (long) users.size());
        userStats.put("active", users.stream().filter(UserAccount::isActive).count());
        userStats.put("disabled", users.stream().filter(u -> !u.isActive()).count());
        userStats.put("locked", users.stream().filter(UserAccount::isAccountLocked).count());
        userStats.put("waitingForEmailVerification",
            users.stream().filter(u -> Boolean.FALSE.equals(u.getEmailVerified())).count());
        userStats.put("newThisWeek",
            users.stream().filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(weekAgo)).count());
        userStats.put("loggedInThisWeek",
            users.stream().filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(weekAgo)).count());

        Map<String, Object> profileStats = new LinkedHashMap<>();
        profileStats.put("students", students.count());
        profileStats.put("faculty", professors.count());

        List<ResearchOpportunity> research = opportunities.findAll();
        Map<String, Object> researchStats = new LinkedHashMap<>();
        researchStats.put("total", (long) research.size());
        researchStats.put("acceptingApplications", research.stream()
            .filter(o -> o.getAvailability() == null || !"closed".equalsIgnoreCase(o.getAvailability()))
            .filter(o -> o.getApplicationDeadline() == null || !o.getApplicationDeadline().isBefore(today))
            .count());
        researchStats.put("postedThisWeek", research.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(weekAgo)).count());

        List<Application> apps = applications.findAll().stream()
            .filter(a -> a.getOpportunityStatus() != Application.OpportunityStatus.BOOKMARKED)
            .toList();
        Map<String, Object> appStats = new LinkedHashMap<>();
        appStats.put("total", (long) apps.size());
        appStats.put("pending", apps.stream()
            .filter(a -> a.getApplicationStatus() == Application.ApplicationStatus.PENDING).count());
        appStats.put("accepted", apps.stream()
            .filter(a -> a.getApplicationStatus() == Application.ApplicationStatus.ACCEPTED
                && a.getOpportunityStatus() != Application.OpportunityStatus.COMPLETED).count());
        appStats.put("notSelected", apps.stream()
            .filter(a -> a.getApplicationStatus() == Application.ApplicationStatus.REJECTED).count());
        appStats.put("completed", apps.stream()
            .filter(a -> a.getOpportunityStatus() == Application.OpportunityStatus.COMPLETED).count());
        appStats.put("thisWeek", apps.stream()
            .filter(a -> a.getAppliedAt() != null && a.getAppliedAt().isAfter(weekAgo)).count());

        Map<String, Object> messageStats = new LinkedHashMap<>();
        messageStats.put("conversations", conversations.count());
        messageStats.put("messages", messages.count());
        messageStats.put("messagesThisWeek", messages.countByCreatedAtAfter(weekAgo));

        List<ProblemReport> allReports = reports.findAll();
        Map<String, Object> reportStats = new LinkedHashMap<>();
        reportStats.put("new", allReports.stream().filter(r -> "NEW".equals(r.getStatus())).count());
        reportStats.put("inProgress", allReports.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).count());
        reportStats.put("fixed", allReports.stream().filter(r -> "FIXED".equals(r.getStatus())).count());

        return new Stats(now, userStats, byRole, profileStats, researchStats, appStats, messageStats, reportStats);
    }
}
