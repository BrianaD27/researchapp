package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.domain.model.Professor;

public interface ProfessorRepositoryInterface {
    // CRUD Operations
    Professor createProfessor(Professor professor);
    Professor getProfessorById(Long id);
    Professor updateProfessor(Professor professor, Long id);
    void deleteProfessor(Long id);
    List<Professor> getAllProfessors();

    // Lookups
    Optional<Professor> getProfessorByEmail(String email);
    boolean existsByEmail(String email);
    List<Professor> getProfessorsByDepartment(String department);
    List<Professor> searchProfessors(String term);

    // Used to enforce "one professor profile per login account": checked before
    // creating a new profile, and to resolve which profile the logged-in user owns.
    Optional<Professor> getProfessorByUserAccountId(Long userAccountId);
}
