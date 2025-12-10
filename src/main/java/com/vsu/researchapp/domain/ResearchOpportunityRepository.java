package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchOpportunityRepository extends JpaRepository<ResearchOpportunity, Long> {
}

