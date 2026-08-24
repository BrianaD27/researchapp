package com.vsu.researchapp.infrastructure.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.Application;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;
import com.vsu.researchapp.domain.model.Application.OpportunityStatus;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByStudentIdAndResearchOpportunityId(Long studentId, Long researchOpportunityId);

    List<Application> findByStudentId(Long studentId);

    List<Application> findByStudentIdAndOpportunityStatus(Long studentId, OpportunityStatus opportunityStatus);

    List<Application> findByStudentIdAndApplicationStatus(Long studentId, ApplicationStatus applicationStatus);

    List<Application> findByResearchOpportunityId(Long researchOpportunityId);

    List<Application> findByResearchOpportunityIdAndApplicationStatus(Long researchOpportunityId, ApplicationStatus applicationStatus);

    List<Application> findByApplicationStatusAndOpportunityStatusNotAndResearchOpportunity_EndDateBefore(
        ApplicationStatus applicationStatus, OpportunityStatus excludedOpportunityStatus, LocalDate endDate);
}
