package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.exception.ProfessorNotFoundException;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;

@Repository
public class ProfessorRepositoryImpl implements ProfessorRepositoryInterface {

    private final ProfessorJpaRepository professorRepository;

    public ProfessorRepositoryImpl(ProfessorJpaRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    @Override
    public Professor createProfessor(Professor professor) {
        return professorRepository.save(professor);
    }

    @Override
    public Professor getProfessorById(Long id) {
        return professorRepository.findById(id)
            .orElseThrow(() -> new ProfessorNotFoundException("There is no professor with the id: " + id));
    }

    @Override
    public Professor updateProfessor(Professor professor) {
        if (!professorRepository.existsById(professor.getId())) {
            throw new ProfessorNotFoundException("There is no professor with the id: " + professor.getId());
        }
        return professorRepository.save(professor);
    }

    @Override
    public void deleteProfessor(Long id) {
        if (!professorRepository.existsById(id)) {
            throw new ProfessorNotFoundException("There is no professor with the id: " + id);
        }
        professorRepository.deleteById(id);
    }

    @Override
    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    @Override
    public Optional<Professor> getProfessorByEmail(String email) {
        return professorRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return professorRepository.existsByEmail(email);
    }

    @Override
    public Optional<Professor> getProfessorByUserAccountId(Long userAccountId) {
        return professorRepository.findByUserAccountId(userAccountId);
    }

    @Override
    public List<Professor> getProfessorsByDepartment(String department) {
        return professorRepository.findByDepartmentIgnoreCase(department);
    }

    @Override
    public List<Professor> searchProfessors(String term) {
        return professorRepository.searchByTerm(term);
    }
}
