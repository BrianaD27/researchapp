package com.vsu.researchapp.application.service;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.AssociateProfile;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateProfileRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.Repository.StudentJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// The name in "Good morning, ___".
// - Faculty: always "Dr. <last name>".
// - Everyone else: the name they chose (e.g. "Mr. Hughes", "Jordan"), or their
//   profile name until they pick one.
@Service
public class GreetingService {

    // Letters (any language), spaces, periods, apostrophes, and hyphens.
    private static final Pattern ALLOWED = Pattern.compile("^[\\p{L}][\\p{L} .'\\-]{0,59}$");

    public record Greeting(String name, boolean canChange, String preferredName) {}

    private final CurrentUserService currentUser;
    private final UserAccountRepository accounts;
    private final ProfessorJpaRepository professors;
    private final StudentJpaRepository students;
    private final AssociateProfileRepository associateProfiles;

    public GreetingService(CurrentUserService currentUser, UserAccountRepository accounts,
            ProfessorJpaRepository professors, StudentJpaRepository students,
            AssociateProfileRepository associateProfiles) {
        this.currentUser = currentUser;
        this.accounts = accounts;
        this.professors = professors;
        this.students = students;
        this.associateProfiles = associateProfiles;
    }

    @Transactional(readOnly = true)
    public Greeting mine() {
        UserAccount me = currentUser.getCurrentUserAccount();
        return new Greeting(nameFor(me), !isFaculty(me), me.getPreferredName());
    }

    @Transactional
    public Greeting update(String preferredName) {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (isFaculty(me)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty are greeted as Dr. and their last name");
        }
        String v = preferredName == null ? "" : preferredName.trim().replaceAll("\\s+", " ");
        if (v.isEmpty()) {
            me.setPreferredName(null);
        } else if (!ALLOWED.matcher(v).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Use up to 60 letters, spaces, periods, apostrophes, or hyphens");
        } else {
            me.setPreferredName(v);
        }
        accounts.save(me);
        return new Greeting(nameFor(me), true, me.getPreferredName());
    }

    public String nameFor(UserAccount me) {
        if (isFaculty(me)) {
            // "Rhonda Goodson" and "Dr. Rhonda Goodson" both become "Dr. Goodson".
            String n = professors.findByUserAccountId(me.getId()).map(Professor::getName).orElse("").trim();
            String[] parts = n.split("\\s+");
            return "Dr. " + parts[parts.length - 1];
        }
        if (me.getPreferredName() != null && !me.getPreferredName().isBlank()) return me.getPreferredName();
        String role = upper(me.getRole());
        String profileName = switch (role) {
            case "STUDENT" -> students.findByUserAccountId(me.getId()).map(Student::getName).orElse(null);
            case "ASSOCIATE" -> associateProfiles.findByUserAccountId(me.getId()).map(AssociateProfile::getName).orElse(null);
            default -> null;
        };
        return profileName == null || profileName.isBlank() ? me.getUsername() : profileName.trim();
    }

    // Professors, and admins who have a faculty profile with a name.
    private boolean isFaculty(UserAccount me) {
        String role = upper(me.getRole());
        if (!"PROFESSOR".equals(role) && !"ADMIN".equals(role)) return false;
        return professors.findByUserAccountId(me.getId())
            .map(Professor::getName).filter(n -> n != null && !n.isBlank()).isPresent();
    }

    private static String upper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
