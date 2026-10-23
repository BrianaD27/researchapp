package com.vsu.researchapp.infrastructure.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.vsu.researchapp.domain.model.SavedStudent;
import com.vsu.researchapp.domain.repositoryinterfaces.SavedStudentRepositoryInterface;

@Repository
public class SavedStudentRepositoryImpl implements SavedStudentRepositoryInterface {

    private final SavedStudentJpaRepository savedStudentRepository;

    public SavedStudentRepositoryImpl(SavedStudentJpaRepository savedStudentRepository) {
        this.savedStudentRepository = savedStudentRepository;
    }

    @Override
    public SavedStudent saveStudent(SavedStudent savedStudent) {
        return savedStudentRepository.save(savedStudent);
    }

    @Override
    public SavedStudent getSavedStudentById(Long id) {
        return savedStudentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("There is no saved student with the id: " + id));
    }

    @Override
    public void unsaveStudentByProfessorIdAndStudentId(Long professorId, Long studentId) {
        savedStudentRepository.deleteByProfessorIdAndStudentId(professorId, studentId);
    }

    @Override
    public List<SavedStudent> getSavedStudentsByProfessorId(Long professorId) {
        return savedStudentRepository.findByProfessorId(professorId);
    }

    @Override
    public boolean isStudentSavedByProfessor(Long professorId, Long studentId) {
        return savedStudentRepository.existsByProfessorIdAndStudentId(professorId, studentId);
    }

    @Override
    public Optional<SavedStudent> getSavedStudentByProfessorIdAndStudentId(Long professorId, Long studentId) {
        return savedStudentRepository.findByProfessorIdAndStudentId(professorId, studentId);
    }
}
