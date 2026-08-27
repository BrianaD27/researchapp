package com.vsu.researchapp.application.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateProfessorDto;
import com.vsu.researchapp.application.dto.ProfessorDto;
import com.vsu.researchapp.application.dto.UpdateProfessorDto;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@Service
public class ProfessorService {

    private final ProfessorRepositoryInterface professorRepository;
    private final CurrentUserService currentUserService;

    public ProfessorService(ProfessorRepositoryInterface professorRepository,
            CurrentUserService currentUserService) {
        this.professorRepository = professorRepository;
        this.currentUserService = currentUserService;
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

        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        boolean isAdmin = currentUserService.isAdmin(currentUser);

        // One professor profile per login account - same reasoning as
        // StudentService.createStudent's duplicate-profile check.
        if (!isAdmin && professorRepository.getProfessorByUserAccountId(currentUser.getId()).isPresent()) {
            throw new IllegalStateException("This account already has a professor profile");
        }

        Professor professor = new Professor();
        professor.setName(dto.name());
        professor.setEmail(dto.email());
        professor.setDepartment(dto.department());
        professor.setOfficeLocation(dto.officeLocation());

        // Owning account comes from the authenticated JWT, not the request body -
        // see StudentService.createStudent for why. Admin-created records are left
        // unlinked since the admin is creating this on someone else's behalf.
        if (!isAdmin) {
            professor.setUserAccountId(currentUser.getId());
        }

        return entityToDto(professorRepository.createProfessor(professor));
    }

    public ProfessorDto updateProfessor(Long id, UpdateProfessorDto updated) {
        Professor professor = professorRepository.getProfessorById(id);
        assertCanModify(professor);

        Optional.ofNullable(updated.name()).ifPresent(professor::setName);
        Optional.ofNullable(updated.department()).ifPresent(professor::setDepartment);
        Optional.ofNullable(updated.officeLocation()).ifPresent(professor::setOfficeLocation);
        Optional.ofNullable(updated.description()).ifPresent(professor::setDescription);
        Optional.ofNullable(updated.profilePictureUrl()).ifPresent(professor::setProfilePictureUrl);

        return entityToDto(professorRepository.updateProfessor(professor, id));
    }

    public void deleteProfessor(Long id) {
        Professor professor = professorRepository.getProfessorById(id);
        assertCanModify(professor);
        professorRepository.deleteProfessor(id);
    }

    // Shared ownership gate for update/delete - same pattern as
    // StudentService.assertCanModify: an ADMIN can touch any professor record,
    // anyone else must be the account that owns this specific profile.
    private void assertCanModify(Professor professor) {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (professor.getUserAccountId() == null
                || !professor.getUserAccountId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only modify your own professor profile");
        }
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
