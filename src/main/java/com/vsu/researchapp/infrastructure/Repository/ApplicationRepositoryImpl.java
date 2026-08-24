package com.vsu.researchapp.infrastructure.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;
import com.vsu.researchapp.domain.model.Application.OpportunityStatus;
import com.vsu.researchapp.domain.repositoryinterfaces.ApplicationRepositoryInterface;

@Repository
public class ApplicationRepositoryImpl implements ApplicationRepositoryInterface {

    private final ApplicationRepository applicationRepository;

    public ApplicationRepositoryImpl(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Override
    public Application createApplication(Application application) {
        return applicationRepository.save(application);
    }

    @Override
    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("There is no application with the id: " + id));
    }

    @Override
    public Application updateApplication(Long id, Application application) {
        if (!applicationRepository.existsById(id)) {
            throw new IllegalArgumentException("There is no application with the id: " + id);
        }
        application.setId(id);
        return applicationRepository.save(application);
    }

    @Override
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new IllegalArgumentException("There is no application with the id: " + id);
        }
        applicationRepository.deleteById(id);
    }

    @Override
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Override
    public Application updateApplicationStatus(Long id, ApplicationStatus applicationStatus) {
        Application application = getApplicationById(id);
        application.setApplicationStatus(applicationStatus);
        return applicationRepository.save(application);
    }

    @Override
    public List<Application> getApplicationsByOpportunityId(Long opportunityId) {
        return applicationRepository.findByResearchOpportunityId(opportunityId);
    }

    @Override
    public List<Application> getApplicationsByOpportunityIdAndStatus(Long opportunityId, ApplicationStatus applicationStatus) {
        return applicationRepository.findByResearchOpportunityIdAndApplicationStatus(opportunityId, applicationStatus);
    }

    @Override
    public List<Application> getApplicationsByStudentId(Long studentId) {
        return applicationRepository.findByStudentId(studentId);
    }

    @Override
    public List<Application> getStudentApplicationsByStatus(Long studentId, ApplicationStatus applicationStatus) {
        return applicationRepository.findByStudentIdAndApplicationStatus(studentId, applicationStatus);
    }

    @Override
    public Optional<Application> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId) {
        return applicationRepository.findByStudentIdAndResearchOpportunityId(studentId, opportunityId);
    }

    @Override
    public List<Application> getAcceptedApplicationsPassEndDate() {
        return applicationRepository.findByApplicationStatusAndOpportunityStatusNotAndResearchOpportunity_EndDateBefore(
            ApplicationStatus.ACCEPTED, OpportunityStatus.COMPLETED, LocalDate.now());
    }
}
