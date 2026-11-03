package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.application.service.CalendarService.Occurrence;
import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.AssociateActivity;
import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.AssociateProfile;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateActivityRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateProfileRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.LoginHistoryRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ApplicationJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.ResearchOpportunityJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// "My Dashboard" for faculty and their Research Associates. Only shows people
// connected to this professor's own projects (their applicants, students, and
// associates), never the whole site. People who turned off "Show when I'm
// active" are left out of the online and active-this-week lists. Associates
// without review permission see counts only, not student names.
@Service
public class FacultyDashboardService {

    public record Tiles(long pendingApplications, long unreadMessages, long openOpportunities,
                        long closingThisWeek, long studentsOnProjects) {}

    public record Person(Long accountId, String name, String role, String detail, boolean onlineNow) {}

    public record Activity(LocalDateTime at, String text) {}

    public record Project(Long id, String title, String availability, LocalDate deadline, long applicants,
                          long pending, long accepted) {}

    public record Dashboard(String professorName, boolean associate, boolean canSeeStudents, Tiles tiles,
                            List<Person> onlineNow, long activeThisWeek, long connectedPeople,
                            List<Occurrence> thisWeek, List<Activity> recent, List<Project> projects) {}

    private final CurrentUserService currentUser;
    private final ProfessorJpaRepository professors;
    private final ResearchOpportunityJpaRepository opportunities;
    private final ApplicationJpaRepository applications;
    private final AssociateLinkRepository links;
    private final AssociateProfileRepository associateProfiles;
    private final AssociateActivityRepository activity;
    private final LoginHistoryRepository logins;
    private final UserAccountRepository accounts;
    private final PresenceService presence;
    private final MessageService messages;
    private final CalendarService calendar;

    public FacultyDashboardService(CurrentUserService currentUser, ProfessorJpaRepository professors,
            ResearchOpportunityJpaRepository opportunities, ApplicationJpaRepository applications,
            AssociateLinkRepository links, AssociateProfileRepository associateProfiles,
            AssociateActivityRepository activity, LoginHistoryRepository logins, UserAccountRepository accounts,
            PresenceService presence, MessageService messages, CalendarService calendar) {
        this.currentUser = currentUser;
        this.professors = professors;
        this.opportunities = opportunities;
        this.applications = applications;
        this.links = links;
        this.associateProfiles = associateProfiles;
        this.activity = activity;
        this.logins = logins;
        this.accounts = accounts;
        this.presence = presence;
        this.messages = messages;
        this.calendar = calendar;
    }

    @Transactional(readOnly = true)
    public Dashboard mine(String actingFor) {
        UserAccount me = currentUser.getCurrentUserAccount();
        String role = me.getRole() == null ? "" : me.getRole().toUpperCase(Locale.ROOT);
        boolean associate = "ASSOCIATE".equals(role);
        Long professorId;
        boolean canSeeStudents;
        if (associate) {
            List<AssociateLink> mine = links.findByAssociateUserAccountIdOrderByIdAsc(me.getId());
            AssociateLink chosen = mine.stream()
                .filter(l -> String.valueOf(l.getProfessorId()).equals(actingFor))
                .findFirst().orElse(mine.isEmpty() ? null : mine.get(0));
            professorId = chosen == null ? null : chosen.getProfessorId();
            canSeeStudents = chosen != null && chosen.isCanReview();
        } else if ("PROFESSOR".equals(role) || "ADMIN".equals(role)) {
            professorId = professors.findByUserAccountId(me.getId()).map(Professor::getId).orElse(null);
            canSeeStudents = true;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "The dashboard is for faculty");
        }

        LocalDate today = LocalDate.now();
        List<Occurrence> thisWeek = calendar.mine(today, today.plusDays(14)).occurrences();
        long unread = messages.unreadCount();
        if (professorId == null) {
            return new Dashboard(null, associate, false, new Tiles(0, unread, 0, 0, 0), List.of(), 0, 0,
                thisWeek, List.of(), List.of());
        }
        Professor professor = professors.findById(professorId).orElse(null);
        String professorName = professor == null ? null : professor.getName();

        List<ResearchOpportunity> opps = opportunities.findByProfessorId(professorId);
        long pending = 0;
        long open = 0;
        long closing = 0;
        Map<Long, Student> students = new LinkedHashMap<>();
        Map<Long, String> studentDetail = new LinkedHashMap<>();
        java.util.Set<Long> working = new java.util.HashSet<>();
        List<Project> projects = new ArrayList<>();
        List<Activity> recent = new ArrayList<>();
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        for (ResearchOpportunity o : opps) {
            boolean isOpen = "open".equalsIgnoreCase(o.getAvailability());
            if (isOpen) open++;
            LocalDate d = o.getApplicationDeadline();
            if (isOpen && d != null && !d.isBefore(today) && !d.isAfter(today.plusDays(7))) closing++;
            long apps = 0, pend = 0, acc = 0;
            for (Application a : applications.findByResearchOpportunityId(o.getId())) {
                if (a.getOpportunityStatus() == Application.OpportunityStatus.BOOKMARKED) continue;
                apps++;
                Student s = a.getStudent();
                if (a.getApplicationStatus() == Application.ApplicationStatus.PENDING) { pend++; pending++; }
                if (a.getApplicationStatus() == Application.ApplicationStatus.ACCEPTED) {
                    acc++;
                    if (a.getOpportunityStatus() != Application.OpportunityStatus.COMPLETED && s != null) {
                        working.add(s.getId());
                    }
                }
                if (s != null && s.getUserAccountId() != null) {
                    students.putIfAbsent(s.getUserAccountId(), s);
                    studentDetail.putIfAbsent(s.getUserAccountId(), o.getTitle());
                }
                if (canSeeStudents && a.getAppliedAt() != null && a.getAppliedAt().isAfter(weekAgo) && s != null) {
                    recent.add(new Activity(a.getAppliedAt(), name(s.getName(), "A student") + " applied to " + o.getTitle()));
                }
            }
            projects.add(new Project(o.getId(), o.getTitle(), o.getAvailability(), d, apps, pend, acc));
        }
        for (AssociateActivity x : activity.findTop50ByProfessorIdOrderByCreatedAtDesc(professorId)) {
            if (x.getCreatedAt() == null || x.getCreatedAt().isBefore(weekAgo)) continue;
            String text = x.getActorUsername() + " " + humanAction(x.getAction())
                + (x.getDetail() == null || x.getDetail().isBlank() ? "" : ": " + x.getDetail());
            recent.add(new Activity(x.getCreatedAt(), text));
        }
        recent.sort(Comparator.comparing(Activity::at).reversed());
        if (recent.size() > 10) recent = new ArrayList<>(recent.subList(0, 10));

        // People connected to this professor: students on or applying to their
        // projects, their associates, and the professor themselves.
        List<Person> people = new ArrayList<>();
        for (Map.Entry<Long, Student> e : students.entrySet()) {
            people.add(new Person(e.getKey(), name(e.getValue().getName(), "Student"), "Student",
                studentDetail.get(e.getKey()), false));
        }
        for (AssociateLink l : links.findByProfessorIdOrderByIdAsc(professorId)) {
            String n = associateProfiles.findByUserAccountId(l.getAssociateUserAccountId())
                .map(AssociateProfile::getName).filter(s -> s != null && !s.isBlank())
                .orElseGet(() -> accounts.findById(l.getAssociateUserAccountId()).map(UserAccount::getUsername).orElse("Associate"));
            people.add(new Person(l.getAssociateUserAccountId(), n, "Research Associate", null, false));
        }
        if (professor != null && professor.getUserAccountId() != null && !professor.getUserAccountId().equals(me.getId())) {
            people.add(new Person(professor.getUserAccountId(), name(professorName, "Professor"), "Professor", null, false));
        }
        people.removeIf(p -> p.accountId().equals(me.getId()));

        List<Person> online = new ArrayList<>();
        long activeWeek = 0;
        for (Person p : people) {
            if (!presence.showActive(p.accountId())) continue;
            boolean now = presence.isActive(p.accountId());
            if (now) online.add(new Person(p.accountId(), p.name(), p.role(), p.detail(), true));
            if (now || loggedInSince(p.accountId(), weekAgo)) activeWeek++;
        }
        if (!canSeeStudents) online.removeIf(p -> "Student".equals(p.role()));

        Tiles tiles = new Tiles(pending, unread, open, closing, working.size());
        projects.sort(Comparator.comparing(Project::title, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return new Dashboard(professorName, associate, canSeeStudents, tiles, online, activeWeek, people.size(),
            thisWeek, canSeeStudents ? recent : List.of(), projects);
    }

    private boolean loggedInSince(Long accountId, LocalDateTime since) {
        String username = accounts.findById(accountId).map(UserAccount::getUsername).orElse(null);
        if (username == null) return false;
        return logins.findTop10ByUsernameOrderByLoginTimeDesc(username).stream()
            .anyMatch(h -> "SUCCESS".equalsIgnoreCase(h.getStatus()) && h.getLoginTime() != null
                && h.getLoginTime().isAfter(since));
    }

    private static String name(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String humanAction(String action) {
        if (action == null) return "did something";
        return action.toLowerCase(Locale.ROOT).replace('_', ' ');
    }
}
