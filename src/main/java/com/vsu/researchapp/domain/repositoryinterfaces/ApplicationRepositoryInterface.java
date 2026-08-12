package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;


public interface  ApplicationRepositoryInterface {
    // CRUD Operations
    Application createApplication(Application application);
    Application getApplicationById(Long id);
    Application updateApplication(Long id, Application application);
    void deleteApplication(Long id);
    
    // Faculty Operations on Applications
    Application updateApplicationStatus(Long id, ApplicationStatus applicationStatus);
    List<Application> getApplicationsByOpportunityId(Long opportunityId);

    // Student Operations on Applications
    List<Application> getStudentApplicationsByStatus(Long studentId, ApplicationStatus applicationStatus);

    // Prevent Duplication Opportunities
    Optional<Application> findByStudentIdAndOpportunityId(Long studentId, Long OpportunityId);

    // Automatic Completed Status - Mark completed when end date passes
    List<Application> getAcceptedApplicationsPassEndDate();





}
