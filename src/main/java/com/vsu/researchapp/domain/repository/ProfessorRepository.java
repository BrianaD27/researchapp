package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.Professor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END Professor p WHERE p.email = :email")
    boolean existsByEmail(String email);
}
