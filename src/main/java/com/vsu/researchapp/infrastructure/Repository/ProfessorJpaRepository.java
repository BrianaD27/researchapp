package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsu.researchapp.domain.model.Professor;

public interface ProfessorJpaRepository extends JpaRepository<Professor, Long> {

    Optional<Professor> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Professor> findByUserAccountId(Long userAccountId);

    List<Professor> findByDepartmentIgnoreCase(String department);

    @Query("SELECT p FROM Professor p WHERE " +
        "LOWER(p.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(p.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(p.department) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Professor> searchByTerm(@Param("term") String term);
}
