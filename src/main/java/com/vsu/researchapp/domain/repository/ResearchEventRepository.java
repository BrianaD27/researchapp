package com.vsu.researchapp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.model.ResearchEvent;

@Repository
public interface ResearchEventRepository extends JpaRepository<ResearchEvent, Long> {
    // Add Custom Queries Here
}
