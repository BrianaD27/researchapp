package com.vsu.researchapp.domain.repositoryinterfaces;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpportunityRepository extends JpaRepository<ResearchOpportunity, Long> {
}
