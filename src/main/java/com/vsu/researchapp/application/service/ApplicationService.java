package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.ApplicationDto;
import com.vsu.researchapp.application.dto.CreateApplicationDto;
import com.vsu.researchapp.application.dto.UpdateApplicationDto;
import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;
import com.vsu.researchapp.domain.model.Application.OpportunityStatus;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repositoryinterfaces.ApplicationRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

@Service
public class ApplicationService {

    private final ApplicationRepositoryInterface applicationRepository;
    private final StudentRepositoryInterface studentRepository;
    private final ResearchOpportunityRepositoryInterface opportunityRepository;
    private final NotificationService notificationService;
    private final AssociateActivityService associateActivity;

    public ApplicationService(
            ApplicationRepositoryInterface applicationRepository,
            StudentRepositoryInterface studentRepository,
            ResearchOpportunityRepositoryInterface opportunityRepository,
            NotificationService notificationService,
            AssociateActivityService associateActivity) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
        this.notificationService = notificationService;
        this.associateActivity = associateActivity;
    }

    // Audit trail entry when a Research Associate acts on a professor's applicant.
    private void recordAssociateAction(Application application, String action) {
        ResearchOpportunity opportunity = application.getResearchOpportunity();
        if (opportunity == null || opportunity.getProfessor() == null) return;
        associateActivity.record(opportunity.getProfessor().getId(), action,
            application.getStudent().getName() + " on \"" + opportunity.getTitle() + "\": "
                + application.getApplicationStatus() + " / " + application.getOpportunityStatus());
    }

    @PreAuthorize("@studentOpportunityAccess.admin()")
    public List<ApplicationDto> getAllApplications() {
        return applicationRepository.getAllApplications().stream().map(this::entityToDto).toList();
    }

    @PreAuthorize("@studentOpportunityAccess.readApplication(#p0)")
    public ApplicationDto getApplicationById(Long id) {
        return entityToDto(applicationRepository.getApplicationById(id));
    }

    @PreAuthorize("@studentOpportunityAccess.student(#p0)")
    public List<ApplicationDto> getApplicationsByStudentId(Long studentId) {
        return applicationRepository.getApplicationsByStudentId(studentId).stream().map(this::entityToDto).toList();
    }

    @PreAuthorize("@studentOpportunityAccess.opportunityApplicants(#p0)")
    public List<ApplicationDto> getApplicationsByOpportunityId(Long opportunityId) {
        return applicationRepository.getApplicationsByOpportunityId(opportunityId).stream().map(this::entityToDto).toList();
    }

    @PreAuthorize("@studentOpportunityAccess.opportunityApplicants(#p0)")
    public List<ApplicationDto> getApplicationsByOpportunityIdAndStatus(Long opportunityId, ApplicationStatus status) {
        return applicationRepository.getApplicationsByOpportunityIdAndStatus(opportunityId, status).stream().map(this::entityToDto).toList();
    }

    @PreAuthorize("@studentOpportunityAccess.student(#p0.studentId())")
    public ApplicationDto applyToOpportunity(CreateApplicationDto dto) {
        if (applicationRepository.findByStudentIdAndOpportunityId(dto.studentId(), dto.researchOpportunityId()).isPresent()) {
            throw new IllegalArgumentException("This student has already applied to this opportunity");
        }

        Student student = studentRepository.getStudentById(dto.studentId());
        ResearchOpportunity opportunity = opportunityRepository.getResearchOpportunity(dto.researchOpportunityId());
        java.time.LocalDate today = java.time.LocalDate.now();
        if (!"Open".equalsIgnoreCase(opportunity.getAvailability())
                || (opportunity.getApplicationOpenDate() != null
                    && today.isBefore(opportunity.getApplicationOpenDate()))
                || (opportunity.getApplicationDeadline() != null
                    && today.isAfter(opportunity.getApplicationDeadline()))) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "This opportunity is not currently accepting applications");
        }

        Application application = new Application();
        application.setStudent(student);
        application.setResearchOpportunity(opportunity);
        application.setOpportunityStatus(OpportunityStatus.APPLIED);
        application.setApplicationStatus(ApplicationStatus.PENDING);

        ApplicationDto created = entityToDto(applicationRepository.createApplication(application));
        notificationService.notify(student.getUserAccountId(), "APPLICATION_RECEIVED",
            "Application received",
            "Your application to \"" + opportunity.getTitle() + "\" was received and is pending review.",
            "/applied-opportunities");
        if (opportunity.getProfessor() != null) {
            notificationService.notify(opportunity.getProfessor().getUserAccountId(), "NEW_APPLICANT",
                "New applicant",
                student.getName() + " applied to \"" + opportunity.getTitle() + "\".",
                "/faculty-applicants");
        }
        return created;
    }

    @PreAuthorize("@studentOpportunityAccess.decideApplication(#p1)")
    public ApplicationDto updateApplication(UpdateApplicationDto updated, Long id) {
        Application application = applicationRepository.getApplicationById(id);

        OpportunityStatus nextOpportunityStatus = updated.opportunityStatus() != null
            ? updated.opportunityStatus() : application.getOpportunityStatus();
        ApplicationStatus nextApplicationStatus = updated.applicationStatus() != null
            ? updated.applicationStatus() : application.getApplicationStatus();
        requireAcceptedForCompletion(nextOpportunityStatus, nextApplicationStatus);

        ApplicationStatus previousDecision = application.getApplicationStatus();
        OpportunityStatus previousStage = application.getOpportunityStatus();
        Optional.ofNullable(updated.opportunityStatus()).ifPresent(application::setOpportunityStatus);
        Optional.ofNullable(updated.applicationStatus()).ifPresent(application::setApplicationStatus);
        if (updated.mentorNote() != null) {
            String note = updated.mentorNote().trim();
            if (note.length() > 1000) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Mentor note must be 1000 characters or fewer.");
            }
            application.setMentorNote(note.isEmpty() ? null : note);
        }

        ApplicationDto saved = entityToDto(applicationRepository.updateApplication(id, application));
        notifyStudentOfChange(application, previousDecision, previousStage);
        recordAssociateAction(application, updated.mentorNote() != null ? "UPDATED_APPLICATION_NOTE" : "UPDATED_APPLICATION");
        return saved;
    }

    // Faculty accepts/rejects/resets an applicant's status
    @PreAuthorize("@studentOpportunityAccess.decideApplication(#p0)")
    public ApplicationDto updateApplicationStatus(Long id, ApplicationStatus status) {
        Application application = applicationRepository.getApplicationById(id);
        requireAcceptedForCompletion(application.getOpportunityStatus(), status);
        ApplicationStatus previousDecision = application.getApplicationStatus();
        OpportunityStatus previousStage = application.getOpportunityStatus();
        Application saved = applicationRepository.updateApplicationStatus(id, status);
        notifyStudentOfChange(saved, previousDecision, previousStage);
        recordAssociateAction(saved, "DECIDED_APPLICATION");
        return entityToDto(saved);
    }

    // Tells the student when a faculty decision or completion changes their application.
    private void notifyStudentOfChange(Application application,
            ApplicationStatus previousDecision, OpportunityStatus previousStage) {
        Long studentAccount = application.getStudent().getUserAccountId();
        String title = application.getResearchOpportunity().getTitle();
        if (application.getOpportunityStatus() == OpportunityStatus.COMPLETED
                && previousStage != OpportunityStatus.COMPLETED) {
            notificationService.notify(studentAccount, "RESEARCH_COMPLETED", "Research completed",
                "\"" + title + "\" was marked completed. It now appears in your research history.",
                "/completed-opportunities");
            return;
        }
        if (application.getApplicationStatus() == previousDecision) return;
        if (application.getApplicationStatus() == ApplicationStatus.ACCEPTED) {
            notificationService.notify(studentAccount, "APPLICATION_ACCEPTED", "You were accepted",
                "Congratulations! You were accepted to \"" + title + "\".",
                "/ongoing-opportunities");
        } else if (application.getApplicationStatus() == ApplicationStatus.REJECTED) {
            notificationService.notify(studentAccount, "APPLICATION_NOT_SELECTED", "Application update",
                "You were not selected for \"" + title + "\". Keep exploring other opportunities.",
                "/applied-opportunities");
        }
    }

    @PreAuthorize("@studentOpportunityAccess.deleteApplication(#p0)")
    public void deleteApplication(Long id) {
        applicationRepository.deleteApplication(id);
    }

    // Flip accepted applications to COMPLETED once the opportunity's end date has passed
    @PreAuthorize("@studentOpportunityAccess.admin()")
    public List<ApplicationDto> completeExpiredAcceptedApplications() {
        return applicationRepository.getAcceptedApplicationsPassEndDate().stream()
            .map(app -> {
                OpportunityStatus previousStage = app.getOpportunityStatus();
                app.setOpportunityStatus(OpportunityStatus.COMPLETED);
                ApplicationDto saved = entityToDto(applicationRepository.updateApplication(app.getId(), app));
                notifyStudentOfChange(app, app.getApplicationStatus(), previousStage);
                return saved;
            })
            .toList();
    }

    // Completed research must stay tied to an accepted application.
    private void requireAcceptedForCompletion(OpportunityStatus opportunityStatus, ApplicationStatus applicationStatus) {
        if (opportunityStatus == OpportunityStatus.COMPLETED && applicationStatus != ApplicationStatus.ACCEPTED) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT,
                "Only accepted applications can be marked completed. Undo completion before changing the decision.");
        }
    }

    private ApplicationDto entityToDto(Application application) {
        return new ApplicationDto(
            application.getId(),
            application.getStudent().getId(),
            application.getStudent().getName(),
            application.getResearchOpportunity().getId(),
            application.getResearchOpportunity().getTitle(),
            application.getOpportunityStatus(),
            application.getApplicationStatus(),
            application.getAppliedAt(),
            application.getUpdatedAt(),
            application.getMentorNote()
        );
    }
}
