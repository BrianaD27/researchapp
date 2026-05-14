package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Professor p WHERE p.email = :email")
    boolean existsByEmail(String email);

    @Query("SELECT p FROM Professor p WHERE " +
        "LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(p.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(p.department) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(p.title) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Professor> searchProfessors(@Param("term") String term);

    List<Professor> findByDepartmentIgnoreCase(String department);
}
