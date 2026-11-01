package com.vsu.researchapp.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.FacultyRequest;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.FacultyRequestRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Faculty sign-up: a professor signs up with an @vsu.edu email and verifies it.
// Admins then get an alert and approve (the account becomes PROFESSOR and gets a
// faculty profile) or decline (the account is deactivated). Until then the
// account has the PENDING_FACULTY role, which can't open anything in the app.
@Service
public class FacultyRequestService {

    private static final Pattern FACULTY_EMAIL =
        Pattern.compile("^[A-Za-z0-9._%+-]+@vsu\\.edu$", Pattern.CASE_INSENSITIVE);
    public static final String PENDING_ROLE = "PENDING_FACULTY";

    public record RequestView(Long userAccountId, String username, String email, String name,
                              String department, boolean emailVerified, LocalDateTime requestedAt) {}

    private final FacultyRequestRepository requests;
    private final UserAccountRepository accounts;
    private final UserAccountService userAccountService;
    private final ProfessorJpaRepository professors;
    private final NotificationService notifications;
    private final CurrentUserService currentUser;

    public FacultyRequestService(FacultyRequestRepository requests, UserAccountRepository accounts,
            UserAccountService userAccountService, ProfessorJpaRepository professors,
            NotificationService notifications, CurrentUserService currentUser) {
        this.requests = requests;
        this.accounts = accounts;
        this.userAccountService = userAccountService;
        this.professors = professors;
        this.notifications = notifications;
        this.currentUser = currentUser;
    }

    @Transactional
    public void signUp(String username, String email, String password, String name, String department) {
        String cleanEmail = email == null ? "" : email.trim();
        if (!FACULTY_EMAIL.matcher(cleanEmail).matches()) {
            throw bad("Faculty accounts need a VSU faculty email ending in @vsu.edu");
        }
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty() || cleanName.length() > 150) throw bad("Enter your full name");
        String cleanDept = department == null ? "" : department.trim();
        if (cleanDept.length() > 150) throw bad("Department is too long");

        UserAccount user = userAccountService.createFacultyRequestUser(
            username == null ? "" : username.trim(), cleanEmail, password);
        FacultyRequest request = new FacultyRequest();
        request.setUserAccountId(user.getId());
        request.setName(cleanName);
        request.setDepartment(cleanDept.isEmpty() ? null : cleanDept);
        requests.save(request);
        userAccountService.startEmailVerification(user);
    }

    // Once a minute: alert every admin about requests whose email is now verified.
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    @Transactional
    public void alertAdmins() {
        List<FacultyRequest> waiting = requests.findByStatusAndAdminsAlertedFalse("PENDING");
        if (waiting.isEmpty()) return;
        List<UserAccount> admins = accounts.findAll().stream()
            .filter(a -> "ADMIN".equalsIgnoreCase(a.getRole()) && a.isActive()).toList();
        for (FacultyRequest r : waiting) {
            UserAccount user = accounts.findById(r.getUserAccountId()).orElse(null);
            if (user == null || Boolean.FALSE.equals(user.getEmailVerified())) continue;
            String text = "Faculty access request: " + r.getName()
                + (r.getDepartment() == null ? "" : " (" + r.getDepartment() + ")")
                + ", " + user.getEmail() + ". Approve or decline it on the Users page.";
            for (UserAccount admin : admins) {
                notifications.notify(admin.getId(), "FACULTY_REQUEST", "New faculty access request", text, "/admin-users");
            }
            r.setAdminsAlerted(true);
            requests.save(r);
        }
    }

    @Transactional(readOnly = true)
    public List<RequestView> pending() {
        return requests.findByStatusOrderByCreatedAtAsc("PENDING").stream()
            .map(r -> accounts.findById(r.getUserAccountId())
                .map(u -> new RequestView(u.getId(), u.getUsername(), u.getEmail(), r.getName(), r.getDepartment(),
                    !Boolean.FALSE.equals(u.getEmailVerified()), r.getCreatedAt()))
                .orElse(null))
            .filter(v -> v != null)
            .toList();
    }

    @Transactional
    public void approve(Long userAccountId) {
        FacultyRequest r = open(userAccountId);
        UserAccount user = accounts.findById(userAccountId).orElseThrow(() -> notFound());
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            throw bad("This person hasn't verified their email yet");
        }
        userAccountService.assignRole(user.getUsername(), "PROFESSOR");

        // Link their faculty profile: reuse an unclaimed profile with the same email
        // (for example a placeholder one), otherwise create a new one.
        if (professors.findByUserAccountId(userAccountId).isEmpty()) {
            Professor p = professors.findByEmail(user.getEmail())
                .filter(existing -> existing.getUserAccountId() == null)
                .orElseGet(Professor::new);
            if (p.getName() == null || p.getName().isBlank()) p.setName(r.getName());
            if (p.getEmail() == null || p.getEmail().isBlank()) p.setEmail(user.getEmail());
            if ((p.getDepartment() == null || p.getDepartment().isBlank()) && r.getDepartment() != null) {
                p.setDepartment(r.getDepartment());
            }
            p.setUserAccountId(userAccountId);
            professors.save(p);
        }

        decide(r, "APPROVED");
        notifications.notify(userAccountId, "FACULTY_APPROVED", "Your faculty account is approved",
            "Your faculty account was approved. Log out and log back in to start using the app.", "/FacultyLogin");
    }

    @Transactional
    public void decline(Long userAccountId) {
        FacultyRequest r = open(userAccountId);
        UserAccount user = accounts.findById(userAccountId).orElseThrow(() -> notFound());
        userAccountService.setActiveStatus(user.getUsername(), false);
        decide(r, "DECLINED");
    }

    private FacultyRequest open(Long userAccountId) {
        FacultyRequest r = requests.findByUserAccountId(userAccountId).orElseThrow(() -> notFound());
        if (!"PENDING".equals(r.getStatus())) throw bad("This request was already handled");
        return r;
    }

    private void decide(FacultyRequest r, String status) {
        r.setStatus(status);
        r.setDecidedAt(LocalDateTime.now());
        r.setDecidedBy(currentUser.getCurrentUserAccount().getUsername());
        requests.save(r);
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
    }
}
