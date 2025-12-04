package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // you can add custom query methods here later if needed
}
