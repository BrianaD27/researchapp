package com.vsu.researchapp.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.StudentApplication;

public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {

    List<StudentApplication> findByOpportunityId(Long opportunityId);

    // ✅ #8: ownership filter
    List<StudentApplication> findByOpportunityIdAndStudentEmailIgnoreCase(Long opportunityId, String studentEmail);
}
