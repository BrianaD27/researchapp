package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.domain.repository.ProfessorRepository;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import com.vsu.researchapp.domain.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service("ownership")
public class OwnershipAuthorizationService {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final ResearchOpportunityRepository opportunityRepository;

    public OwnershipAuthorizationService(
            StudentRepository studentRepository,
            ProfessorRepository professorRepository,
            ResearchOpportunityRepository opportunityRepository) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.opportunityRepository = opportunityRepository;
    }

    public boolean canManageStudent(Long studentId, String username) {
        return studentRepository.findById(studentId)
            .map(student -> username.equalsIgnoreCase(student.getEmail()))
            .orElse(false);
    }

    public boolean canManageProfessor(Long professorId, String username) {
        return professorRepository.findById(professorId)
            .map(professor -> username.equalsIgnoreCase(professor.getEmail()))
            .orElse(false);
    }

    public boolean canManageOpportunity(Long opportunityId, String username) {
        return opportunityRepository.findById(opportunityId)
            .map(opportunity -> opportunity.getCreatedBy() != null
                && username.equalsIgnoreCase(
                    opportunity.getCreatedBy().getEmail()))
            .orElse(false);
    }
}
