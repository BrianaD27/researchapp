package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    // extra custom query methods can go here later if you need them
}
