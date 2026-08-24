package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

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

    public List<ApplicationDto> getAllApplications() {
        return applicationRepository.getAllApplications().stream().map(this::entityToDto).toList();
    }

    public ApplicationDto getApplicationById(Long id) {
        return entityToDto(applicationRepository.getApplicationById(id));
    }

    public List<ApplicationDto> getApplicationsByStudentId(Long studentId) {
        return applicationRepository.getApplicationsByStudentId(studentId).stream().map(this::entityToDto).toList();
    }

    public List<ApplicationDto> getApplicationsByOpportunityId(Long opportunityId) {
        return applicationRepository.getApplicationsByOpportunityId(opportunityId).stream().map(this::entityToDto).toList();
    }

    public List<ApplicationDto> getApplicationsByOpportunityIdAndStatus(Long opportunityId, ApplicationStatus status) {
        return applicationRepository.getApplicationsByOpportunityIdAndStatus(opportunityId, status).stream().map(this::entityToDto).toList();
    }

    public ApplicationDto applyToOpportunity(CreateApplicationDto dto) {
        if (applicationRepository.findByStudentIdAndOpportunityId(dto.studentId(), dto.researchOpportunityId()).isPresent()) {
            throw new IllegalArgumentException("This student has already applied to this opportunity");
        }

        Student student = studentRepository.getStudentById(dto.studentId());
        ResearchOpportunity opportunity = opportunityRepository.getResearchOpportunity(dto.researchOpportunityId());

        Application application = new Application();
        application.setStudent(student);
        application.setResearchOpportunity(opportunity);
        application.setOpportunityStatus(OpportunityStatus.APPLIED);
        application.setApplicationStatus(ApplicationStatus.PENDING);

        return entityToDto(applicationRepository.createApplication(application));
    }

    public ApplicationDto updateApplication(UpdateApplicationDto updated, Long id) {
        Application application = applicationRepository.getApplicationById(id);

        Optional.ofNullable(updated.opportunityStatus()).ifPresent(application::setOpportunityStatus);
        Optional.ofNullable(updated.applicationStatus()).ifPresent(application::setApplicationStatus);

        return entityToDto(applicationRepository.updateApplication(id, application));
    }

    // Faculty accepts/rejects/resets an applicant's status
    public ApplicationDto updateApplicationStatus(Long id, ApplicationStatus status) {
        return entityToDto(applicationRepository.updateApplicationStatus(id, status));
    }

    public void deleteApplication(Long id) {
        applicationRepository.deleteApplication(id);
    }

    // Flip accepted applications to COMPLETED once the opportunity's end date has passed
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
