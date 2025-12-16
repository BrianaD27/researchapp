package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.repository.ProfessorRepository;

import org.springframework.stereotype.Service;

import java.util.List;

import com.vsu.researchapp.application.dto.ProfessorDto;

@Service
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<ProfessorDto> getAllProfessors() {
        List<Professor> professors = professorRepository.findAll();

        return professors.stream().map(this::entityToDto).toList();

    }


    public Professor getProfessorById(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found: " + id));
    }

    public Professor createProfessor(Professor professor) {
        return professorRepository.save(professor);
    }

    public Professor updateProfessor(Long id, Professor updated) {
        Professor existing = getProfessorById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setDepartment(updated.getDepartment());
        existing.setTitle(updated.getTitle());
        return professorRepository.save(existing);
    }

    public void deleteProfessor(Long id) {
        professorRepository.deleteById(id);
    }

    private ProfessorDto entityToDto(Professor professor) {
        return new ProfessorDto(
            professor.getId(),
            professor.getName(),
            professor.getEmail(),
            professor.getDepartment(),
            professor.getTitle()
        );
    }
}
