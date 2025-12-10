package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.StudentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentApplicationRepository extends JpaRepository<StudentApplication, Long> {
    List<StudentApplication> findByOpportunityId(Long opportunityId);
}
