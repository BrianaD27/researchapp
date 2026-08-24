package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateProfessorDto;
import com.vsu.researchapp.application.dto.ProfessorDto;
import com.vsu.researchapp.application.dto.UpdateProfessorDto;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;

@Service
public class ProfessorService {

    private final ProfessorRepositoryInterface professorRepository;

    public ProfessorService(ProfessorRepositoryInterface professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<ProfessorDto> getAllProfessors() {
        return professorRepository.getAllProfessors().stream().map(this::entityToDto).toList();
    }

    public ProfessorDto getProfessorById(Long id) {
        return entityToDto(professorRepository.getProfessorById(id));
    }

    public List<ProfessorDto> searchProfessors(String term) {
        return professorRepository.searchProfessors(term).stream().map(this::entityToDto).toList();
    }

    public List<ProfessorDto> getProfessorsByDepartment(String department) {
        return professorRepository.getProfessorsByDepartment(department).stream().map(this::entityToDto).toList();
    }

    public ProfessorDto createProfessor(CreateProfessorDto dto) {
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
        professor.setOfficeLocation(dto.officeLocation());

        return entityToDto(professorRepository.createProfessor(professor));
    }

    public ProfessorDto updateProfessor(Long id, UpdateProfessorDto updated) {
        Professor professor = professorRepository.getProfessorById(id);

        Optional.ofNullable(updated.name()).ifPresent(professor::setName);
        Optional.ofNullable(updated.department()).ifPresent(professor::setDepartment);
        Optional.ofNullable(updated.officeLocation()).ifPresent(professor::setOfficeLocation);
        Optional.ofNullable(updated.description()).ifPresent(professor::setDescription);
        Optional.ofNullable(updated.profilePictureUrl()).ifPresent(professor::setProfilePictureUrl);

        return entityToDto(professorRepository.updateProfessor(professor));
    }

    public void deleteProfessor(Long id) {
        professorRepository.deleteProfessor(id);
    }

    private ProfessorDto entityToDto(Professor professor) {
        return new ProfessorDto(
            professor.getId(),
            professor.getName(),
            professor.getEmail(),
            professor.getDepartment(),
            professor.getOfficeLocation(),
            professor.getDescription(),
            professor.getProfilePictureUrl(),
            professor.getCreatedAt(),
            professor.getUpdatedAt()
        );
    }
}
