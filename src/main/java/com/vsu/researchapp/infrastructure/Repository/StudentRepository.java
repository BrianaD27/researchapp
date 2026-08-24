package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsu.researchapp.domain.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

    Optional<Student> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT s FROM Student s WHERE " +
        "LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(s.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(s.major) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(s.classification) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Student> searchByTerm(@Param("term") String term);
}
