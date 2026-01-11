package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchEvent;
import com.vsu.researchapp.domain.repository.ProfessorRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    public ProfessorDto getProfessorById(Long id) {
        Professor professor = professorRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no Professor with the id: " + id));

        return entityToDto(professor);
    }


    public ProfessorDto createProfessor(ProfessorDto dto) {

        //Validate Email
        if (!dto.email().endsWith("@vsu.edu")) {
            throw new IllegalArgumentException("A valid VSU email is required");
        }

        Professor professor = new Professor();
        professor.setName(dto.name());
        professor.setEmail(dto.email());
        professor.setDepartment(dto.department());
        professor.setTitle(dto.title());

       Professor saved = professorRepository.save(professor);
       return entityToDto(saved);
    }

    public ProfessorDto updateProfessor(Long id, ProfessorDto updated) {
        Professor professor = professorRepository.findById(id).orElseThrow(() -> new RuntimeException("There is no Professor with the id: " + id));

        //Validate Email
        if (!updated.email().endsWith("@vsu.edu")) {
            throw new IllegalArgumentException("A valid VSU email is required");
        }

        Optional.ofNullable(updated.name()).ifPresent(professor::setName);
        Optional.ofNullable(updated.email()).ifPresent(professor::setEmail);
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
            professor.getTitle()
        );
    }
}
