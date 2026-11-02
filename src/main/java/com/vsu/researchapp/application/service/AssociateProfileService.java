package com.vsu.researchapp.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.application.dto.EducationDto;
import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.AssociateProfile;
import com.vsu.researchapp.domain.model.EducationEntry;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateProfileRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.FileStorageInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;
import com.vsu.researchapp.infrastructure.Repository.ProfessorJpaRepository;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

// Research Associates' own profiles. Anyone signed in to the VSU app can view one;
// only the associate can edit their own. Editing a profile never changes what the
// associate is allowed to do on a professor's team (that's AssociateLink).
@Service
public class AssociateProfileService {

    private static final Set<String> PHOTO_TYPES = Set.of("image/png", "image/jpeg");
    private static final long PHOTO_MAX = 5L * 1024 * 1024;

    public record WorksWith(Long professorId, String professorName, String department) {}

    public record ProfileView(Long userAccountId, String username, String name, String title, String department,
                              String about, String profilePictureUrl, String outsideInterests,
                              List<EducationDto> education, List<WorksWith> worksWith, boolean onTeam,
                              boolean mine) {}

    public record TeamMember(Long userAccountId, String name, String title, String profilePictureUrl) {}

    private final AssociateProfileRepository profiles;
    private final AssociateLinkRepository links;
    private final UserAccountRepository accounts;
    private final ProfessorJpaRepository professors;
    private final CurrentUserService currentUser;
    private final FileStorageInterface fileStorage;

    public AssociateProfileService(AssociateProfileRepository profiles, AssociateLinkRepository links,
            UserAccountRepository accounts, ProfessorJpaRepository professors, CurrentUserService currentUser,
            FileStorageInterface fileStorage) {
        this.profiles = profiles;
        this.links = links;
        this.accounts = accounts;
        this.professors = professors;
        this.currentUser = currentUser;
        this.fileStorage = fileStorage;
    }

    @Transactional(readOnly = true)
    public ProfileView mine() {
        UserAccount me = requireAssociate();
        return view(me, true);
    }

    @Transactional(readOnly = true)
    public ProfileView byAccount(Long userAccountId) {
        UserAccount account = accounts.findById(userAccountId)
            .filter(a -> "ASSOCIATE".equalsIgnoreCase(a.getRole()) && a.isActive())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return view(account, account.getId().equals(currentUser.getCurrentUserAccount().getId()));
    }

    @Transactional
    public ProfileView update(String name, String title, String department, String about,
            String outsideInterests, List<EducationDto> education) {
        UserAccount me = requireAssociate();
        AssociateProfile p = profileFor(me);
        p.setName(clean(name, 150, "Name"));
        p.setTitle(clean(title, 150, "Title"));
        p.setDepartment(clean(department, 150, "Department"));
        p.setAbout(clean(about, 2000, "About"));
        p.setOutsideInterests(clean(outsideInterests, 1000, "Outside the classroom"));
        if (education != null) {
            if (education.size() > 10) throw bad("Add up to 10 degrees");
            List<EducationEntry> degrees = new ArrayList<>();
            for (EducationDto e : education) {
                if (e == null) continue;
                String degree = clean(e.degree(), 150, "Degree");
                String school = clean(e.school(), 150, "School");
                String year = clean(e.graduationYear(), 20, "Year");
                if (degree == null && school == null && year == null) continue;
                if (degree == null || school == null) throw bad("Each degree needs both a degree name and a school");
                degrees.add(new EducationEntry(degree, school, year, Boolean.TRUE.equals(e.inProgress())));
            }
            p.getEducation().clear();
            p.getEducation().addAll(degrees);
        }
        profiles.save(p);
        return view(me, true);
    }

    @Transactional
    public ProfileView uploadPhoto(MultipartFile file) throws IOException {
        UserAccount me = requireAssociate();
        if (file == null || file.isEmpty()) throw bad("Choose a photo first");
        if (file.getSize() > PHOTO_MAX) throw bad("Photos can be up to 5 MB");
        if (file.getContentType() == null || !PHOTO_TYPES.contains(file.getContentType()) || !looksLikeImage(file)) {
            throw bad("Use a JPG or PNG photo");
        }
        AssociateProfile p = profileFor(me);
        String url = fileStorage.store(file, "profile-pictures");
        String old = p.getProfilePictureUrl();
        p.setProfilePictureUrl(url);
        profiles.save(p);
        if (old != null && !old.isBlank()) fileStorage.delete(old);
        return view(me, true);
    }

    @Transactional
    public ProfileView deletePhoto() {
        UserAccount me = requireAssociate();
        AssociateProfile p = profileFor(me);
        String old = p.getProfilePictureUrl();
        p.setProfilePictureUrl(null);
        profiles.save(p);
        if (old != null && !old.isBlank()) fileStorage.delete(old);
        return view(me, true);
    }

    // The associates on a professor's team, for students and faculty to see on projects.
    @Transactional(readOnly = true)
    public List<TeamMember> team(Long professorId) {
        return links.findByProfessorIdOrderByIdAsc(professorId).stream()
            .map(link -> accounts.findById(link.getAssociateUserAccountId())
                .filter(UserAccount::isActive)
                .map(a -> {
                    AssociateProfile p = profiles.findByUserAccountId(a.getId()).orElse(null);
                    return new TeamMember(a.getId(), displayName(a, p), p == null ? null : p.getTitle(),
                        p == null ? null : p.getProfilePictureUrl());
                })
                .orElse(null))
            .filter(m -> m != null)
            .toList();
    }

    private ProfileView view(UserAccount account, boolean mine) {
        AssociateProfile p = profiles.findByUserAccountId(account.getId()).orElse(null);
        List<WorksWith> worksWith = links.findByAssociateUserAccountIdOrderByIdAsc(account.getId()).stream()
            .map(AssociateLink::getProfessorId)
            .map(id -> professors.findById(id).orElse(null))
            .filter(prof -> prof != null)
            .map((Professor prof) -> new WorksWith(prof.getId(), prof.getName(), prof.getDepartment()))
            .toList();
        return new ProfileView(account.getId(), account.getUsername(), displayName(account, p),
            p == null ? null : p.getTitle(), p == null ? null : p.getDepartment(), p == null ? null : p.getAbout(),
            p == null ? null : p.getProfilePictureUrl(), p == null ? null : p.getOutsideInterests(),
            p == null || p.getEducation() == null ? List.of()
                : p.getEducation().stream()
                    .map(e -> new EducationDto(e.getDegree(), e.getSchool(), e.getGraduationYear(), e.getInProgress()))
                    .toList(),
            worksWith, !worksWith.isEmpty(), mine);
    }

    private AssociateProfile profileFor(UserAccount me) {
        return profiles.findByUserAccountId(me.getId()).orElseGet(() -> {
            AssociateProfile created = new AssociateProfile();
            created.setUserAccountId(me.getId());
            return created;
        });
    }

    private UserAccount requireAssociate() {
        UserAccount me = currentUser.getCurrentUserAccount();
        if (!"ASSOCIATE".equalsIgnoreCase(me.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Research Associates have this profile");
        }
        return me;
    }

    private static String displayName(UserAccount a, AssociateProfile p) {
        return p != null && p.getName() != null && !p.getName().isBlank() ? p.getName() : a.getUsername();
    }

    private static String clean(String value, int max, String label) {
        if (value == null) return null;
        String v = value.trim();
        if (v.length() > max) throw bad(label + " is too long (up to " + max + " characters)");
        return v.isEmpty() ? null : v;
    }

    // Checks the file really starts like a JPG or PNG, not just that it says so.
    private static boolean looksLikeImage(MultipartFile file) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] head = in.readNBytes(8);
            boolean jpg = head.length >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
            boolean png = head.length >= 8 && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G';
            return jpg || png;
        }
    }

    private static ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
