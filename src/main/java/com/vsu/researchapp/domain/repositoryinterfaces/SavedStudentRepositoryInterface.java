package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.SavedStudent;

public interface SavedStudentRepositoryInterface {
    SavedStudent saveStudent(SavedStudent savedStudent);
    SavedStudent getSavedStudentById(Long id);
    void unsaveStudentByProfessorIdAndStudentId(Long professorId, Long studentId);
    List<SavedStudent> getSavedStudentsByProfessorId(Long professorId);

    // Prevent bookmarking the same student twice
    boolean isStudentSavedByProfessor(Long professorId, Long studentId);
    Optional<SavedStudent> getSavedStudentByProfessorIdAndStudentId(Long professorId, Long studentId);
}
