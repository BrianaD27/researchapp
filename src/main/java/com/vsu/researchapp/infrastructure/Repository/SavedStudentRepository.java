package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.SavedStudent;

public interface SavedStudentRepository extends JpaRepository<SavedStudent, Long> {

    List<SavedStudent> findByProfessorId(Long professorId);

    Optional<SavedStudent> findByProfessorIdAndStudentId(Long professorId, Long studentId);

    boolean existsByProfessorIdAndStudentId(Long professorId, Long studentId);

    void deleteByProfessorIdAndStudentId(Long professorId, Long studentId);
}
