package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.PartnerSchool;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.PartnerSchoolRepository;
import com.vsu.researchapp.infrastructure.Repository.ResearchOpportunityJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Partner schools, Level 1: people from an active partner school sign up with their
// own .edu email (role PARTNER) and can only view VSU research, read-only. They
// can't apply, save, message, or see student information.
@Service
public class PartnerSchoolService {

    private static final Pattern VSU_EMAIL =
        Pattern.compile("^[A-Za-z0-9._%+-]+@(students\\.)?vsu\\.edu$", Pattern.CASE_INSENSITIVE);
    private static final Pattern DOMAIN = Pattern.compile("^([a-z0-9-]+\\.)+edu$");

    public record SchoolDto(Long id, String name, String emailDomain, boolean active) {}

    public record PublicSchoolDto(String name, String emailDomain) {}

    public record ResearchDto(Long id, String title, String department, String description,
                              String professorName, String professorDepartment,
                              LocalDate applicationDeadline, LocalDate startDate, LocalDate endDate,
                              List<String> requiredMajors, List<String> requiredSkills) {}

    public record MeDto(String username, String schoolName) {}

    private final PartnerSchoolRepository schools;
    private final ResearchOpportunityJpaRepository opportunities;
    private final CurrentUserService currentUser;

    public PartnerSchoolService(PartnerSchoolRepository schools, ResearchOpportunityJpaRepository opportunities,
            CurrentUserService currentUser) {
        this.schools = schools;
        this.opportunities = opportunities;
        this.currentUser = currentUser;
    }

    public static boolean isVsuEmail(String email) {
        return email != null && VSU_EMAIL.matcher(email.trim()).matches();
    }

    // The active partner school an email belongs to (exact domain or a subdomain of it).
    public Optional<PartnerSchool> schoolFor(String email) {
        if (email == null || !email.contains("@")) return Optional.empty();
        String domain = email.trim().substring(email.trim().lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
        return schools.findByActiveTrueOrderByNameAsc().stream()
            .filter(s -> domain.equals(s.getEmailDomain()) || domain.endsWith("." + s.getEmailDomain()))
            .findFirst();
    }

    public PartnerSchool requireSchoolFor(String email) {
        return schoolFor(email).orElseThrow(() -> new IllegalArgumentException(
            "Sign up with a VSU email, or an email from one of VSU's partner schools"));
    }

    // ---- what partners see ----

    public MeDto me() {
        UserAccount me = currentUser.getCurrentUserAccount();
        String school = schoolFor(me.getEmail()).map(PartnerSchool::getName).orElse(null);
        if (isPartner(me) && school == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Your school's partner access is turned off");
        }
        return new MeDto(me.getUsername(), school == null ? "Virginia State University" : school);
    }

    @Transactional(readOnly = true)
    public List<ResearchDto> research() {
        me();
        return opportunities.findAll().stream()
            .sorted(Comparator.comparing(ResearchOpportunity::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())))
            .map(this::toResearch)
            .toList();
    }

    // ---- admin ----

    public List<SchoolDto> all() {
        return schools.findAllByOrderByNameAsc().stream().map(this::toDto).toList();
    }

    public List<PublicSchoolDto> activePublic() {
        return schools.findByActiveTrueOrderByNameAsc().stream()
            .map(s -> new PublicSchoolDto(s.getName(), s.getEmailDomain())).toList();
    }

    public SchoolDto add(String name, String emailDomain) {
        String cleanName = name == null ? "" : name.trim();
        String domain = emailDomain == null ? "" : emailDomain.trim().toLowerCase(Locale.ROOT).replaceFirst("^@", "");
        if (cleanName.isEmpty() || cleanName.length() > 150) throw bad("Enter the school's name");
        if (!DOMAIN.matcher(domain).matches() || domain.length() > 100) {
            throw bad("Enter the school's email domain, like vt.edu");
        }
        if (domain.equals("vsu.edu") || domain.endsWith(".vsu.edu")) throw bad("VSU is not a partner school");
        if (schools.findByEmailDomainIgnoreCase(domain).isPresent()) throw bad("That email domain is already listed");
        PartnerSchool school = new PartnerSchool();
        school.setName(cleanName);
        school.setEmailDomain(domain);
        return toDto(schools.save(school));
    }

    public SchoolDto setActive(Long id, boolean active) {
        PartnerSchool school = schools.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));
        school.setActive(active);
        return toDto(schools.save(school));
    }

    private static boolean isPartner(UserAccount user) {
        return "PARTNER".equalsIgnoreCase(user.getRole());
    }

    private ResearchDto toResearch(ResearchOpportunity o) {
        Professor p = o.getProfessor();
        return new ResearchDto(o.getId(), o.getTitle(), o.getDepartment(), o.getDescription(),
            p == null ? null : p.getName(), p == null ? null : p.getDepartment(),
            o.getApplicationDeadline(), o.getStartDate(), o.getEndDate(),
            o.getRequiredMajors() == null ? List.of() : List.copyOf(o.getRequiredMajors()),
            o.getRequiredSkills() == null ? List.of() : List.copyOf(o.getRequiredSkills()));
    }

    private SchoolDto toDto(PartnerSchool s) {
        return new SchoolDto(s.getId(), s.getName(), s.getEmailDomain(), s.isActive());
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
