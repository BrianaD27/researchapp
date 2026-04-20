package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import com.vsu.researchapp.domain.model.Professor;

public interface ProfessorRepositoryInterface {
    Professor createProfessor(Professor professor);
    Professor getProfessorById(Long id);
    Professor updateProfessor(Professor professor);
    void deleteProfessor(Long id);
    List<Professor> getAllProfessors();
}
