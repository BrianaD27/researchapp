package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.repository.ProfessorRepository;

import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

import com.vsu.researchapp.application.dto.CreateProfessorDto;
import com.vsu.researchapp.application.dto.ProfessorDto;
import com.vsu.researchapp.application.dto.UpdateProfessorDto;

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

    public ProfessorDto getProfessorById(Long id) {
        Professor professor = professorRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no Professor with the id: " + id));
        return entityToDto(professor);
    }

    public ProfessorDto createProfessor(CreateProfessorDto dto) {
        //Validate Email
        if (!dto.email().endsWith("@vsu.edu")) {
            throw new IllegalArgumentException("A valid VSU email is required");
        }
        if (professorRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("A professor with this email already exists");
        }

        Professor professor = new Professor();
        professor.setName(dto.name());
        professor.setEmail(dto.email());
        professor.setDepartment(dto.department());
        professor.setTitle(dto.title());

       Professor saved = professorRepository.save(professor);
       return entityToDto(saved);
    }

    public ProfessorDto updateProfessor(Long id, UpdateProfessorDto updated) {
        Professor professor = professorRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no Professor with the id: " + id));

        Optional.ofNullable(updated.name()).ifPresent(professor::setName);
        Optional.ofNullable(updated.department()).ifPresent(professor::setDepartment);
        Optional.ofNullable(updated.title()).ifPresent(professor::setTitle);

        Professor saved = professorRepository.save(professor);
        return entityToDto(saved);
    }

    public void deleteProfessor(Long id) {
        if (!professorRepository.existsById(id)) {
            throw new RuntimeException("There is no Professor with the id: " + id);
        }

        professorRepository.deleteById(id);
    }

    private ProfessorDto entityToDto(Professor professor) {
        return new ProfessorDto(
            professor.getId(),
            professor.getName(),
            professor.getEmail(),
            professor.getDepartment(),
            professor.getTitle(),
            professor.getCreatedAt(),
            professor.getUpdatedAt()
        );
    }
}
