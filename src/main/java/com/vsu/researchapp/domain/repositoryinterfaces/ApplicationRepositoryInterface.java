package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;

public interface ApplicationRepositoryInterface {
    // CRUD Operations
    Application createApplication(Application application);
    Application getApplicationById(Long id);
    Application updateApplication(Long id, Application application);
    void deleteApplication(Long id);
    List<Application> getAllApplications();

    // Faculty Operations on Applications
    Application updateApplicationStatus(Long id, ApplicationStatus applicationStatus);
    List<Application> getApplicationsByOpportunityId(Long opportunityId);
    List<Application> getApplicationsByOpportunityIdAndStatus(Long opportunityId, ApplicationStatus applicationStatus);

    // Student Operations on Applications
    List<Application> getApplicationsByStudentId(Long studentId);
    List<Application> getStudentApplicationsByStatus(Long studentId, ApplicationStatus applicationStatus);

    // Prevent Duplicate Applications
    Optional<Application> findByStudentIdAndOpportunityId(Long studentId, Long opportunityId);

    // Automatic Completed Status - Mark completed when end date passes
    List<Application> getAcceptedApplicationsPassEndDate();
}
