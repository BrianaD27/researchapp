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

    public ApplicationService(
            ApplicationRepositoryInterface applicationRepository,
            StudentRepositoryInterface studentRepository,
            ResearchOpportunityRepositoryInterface opportunityRepository) {
        this.applicationRepository = applicationRepository;
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
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

    @PreAuthorize("@studentOpportunityAccess.opportunity(#p0)")
    public List<ApplicationDto> getApplicationsByOpportunityId(Long opportunityId) {
        return applicationRepository.getApplicationsByOpportunityId(opportunityId).stream().map(this::entityToDto).toList();
    }

    @PreAuthorize("@studentOpportunityAccess.opportunity(#p0)")
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

        return entityToDto(applicationRepository.createApplication(application));
    }

    @PreAuthorize("@studentOpportunityAccess.decideApplication(#p1)")
    public ApplicationDto updateApplication(UpdateApplicationDto updated, Long id) {
        Application application = applicationRepository.getApplicationById(id);

        Optional.ofNullable(updated.opportunityStatus()).ifPresent(application::setOpportunityStatus);
        Optional.ofNullable(updated.applicationStatus()).ifPresent(application::setApplicationStatus);

        return entityToDto(applicationRepository.updateApplication(id, application));
    }

    // Faculty accepts/rejects/resets an applicant's status
    @PreAuthorize("@studentOpportunityAccess.decideApplication(#p0)")
    public ApplicationDto updateApplicationStatus(Long id, ApplicationStatus status) {
        return entityToDto(applicationRepository.updateApplicationStatus(id, status));
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
                app.setOpportunityStatus(OpportunityStatus.COMPLETED);
                return entityToDto(applicationRepository.updateApplication(app.getId(), app));
            })
            .toList();
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
            application.getUpdatedAt()
        );
    }
}
