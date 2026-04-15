package com.vsu.researchapp.domain.repositoryinterfaces;

import com.vsu.researchapp.domain.model.Student;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    @Query("SELECT s FROM Student s WHERE " + "LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " + "LOWER(s.major) LIKE LOWER(CONCAT('%', :term, '%')) OR " + "LOWER(s.skills) LIKE LOWER(CONCAT('%', :term, '%')) OR " + "CAST(s.graduateYear AS string) LIKE CONCAT('%', :term, '%')")
    List<Student> searchStudents(@Param("term") String term);
} 
