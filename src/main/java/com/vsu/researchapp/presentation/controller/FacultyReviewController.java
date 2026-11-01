package com.vsu.researchapp.presentation.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.AssociateLink;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.AssociateLinkRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.ApplicationRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@RestController
@RequestMapping("/api/faculty-review")
public class FacultyReviewController {
    private final CurrentUserService currentUser;
    private final ProfessorRepositoryInterface professors;
    private final ApplicationRepositoryInterface applications;
    private final AssociateLinkRepository associateLinks;
    private final Path resumeDirectory;

    public FacultyReviewController(
            CurrentUserService currentUser,
            ProfessorRepositoryInterface professors,
            ApplicationRepositoryInterface applications,
            AssociateLinkRepository associateLinks,
            @Value("${resume.storage.path:./private-uploads/student-resumes}") String storagePath) {
        this.currentUser = currentUser;
        this.professors = professors;
        this.applications = applications;
        this.associateLinks = associateLinks;
        this.resumeDirectory = Path.of(storagePath).toAbsolutePath().normalize();
    }

    // professorId is whose dashboard to show. For a Research Associate it is the
    // professor picked in "Working for" (sent as X-Acting-For), if they are on
    // that professor's team; otherwise their first professor. This only chooses
    // what to display: every action is still checked against the team permissions.
    public record ReviewContext(boolean admin, Long professorId, boolean associate,
                                boolean canPublish, boolean canReview, String actingForName) {}

    @GetMapping("/context")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR', 'ASSOCIATE')")
    @Transactional(readOnly = true)
    public ReviewContext context(@RequestHeader(value = "X-Acting-For", required = false) String actingFor) {
        UserAccount account = currentUser.getCurrentUserAccount();
        boolean admin = currentUser.isAdmin(account);
        if ("ASSOCIATE".equalsIgnoreCase(account.getRole())) {
            java.util.List<AssociateLink> mine = associateLinks.findByAssociateUserAccountIdOrderByIdAsc(account.getId());
            AssociateLink chosen = mine.stream()
                .filter(link -> String.valueOf(link.getProfessorId()).equals(actingFor))
                .findFirst()
                .orElse(mine.isEmpty() ? null : mine.get(0));
            if (chosen == null) {
                return new ReviewContext(false, null, true, false, false, null);
            }
            String name = professors.getProfessorById(chosen.getProfessorId()).getName();
            return new ReviewContext(false, chosen.getProfessorId(), true,
                chosen.isCanPublish(), chosen.isCanReview(), name);
        }
        Long professorId = professors.getProfessorByUserAccountId(account.getId())
            .map(professor -> professor.getId())
            .orElse(null);
        return new ReviewContext(admin, professorId, false, true, true, null);
    }

    @GetMapping("/applications/{applicationId}/resume")
    @PreAuthorize("@studentOpportunityAccess.decideApplication(#p0)")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> downloadResume(
            @PathVariable Long applicationId) throws IOException {
        Long studentId = applications.getApplicationById(applicationId)
            .getStudent().getId();
        Path file = resumeDirectory.resolve(studentId + ".pdf").normalize();

        if (!file.startsWith(resumeDirectory) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "This applicant has not uploaded a resume");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(Files.readAllBytes(file));
    }
}
