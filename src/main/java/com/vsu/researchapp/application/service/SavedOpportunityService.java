package com.vsu.researchapp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateSavedOpportunityDto;
import com.vsu.researchapp.application.dto.SavedOpportunityDto;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.SavedOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.SavedOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;

// Thin by design: a bookmark has no business rules beyond "don't save it twice."
@Service
public class SavedOpportunityService {

    private final SavedOpportunityRepositoryInterface savedOpportunityRepository;
    private final StudentRepositoryInterface studentRepository;
    private final ResearchOpportunityRepositoryInterface opportunityRepository;

    public SavedOpportunityService(
            SavedOpportunityRepositoryInterface savedOpportunityRepository,
            StudentRepositoryInterface studentRepository,
            ResearchOpportunityRepositoryInterface opportunityRepository) {
        this.savedOpportunityRepository = savedOpportunityRepository;
        this.studentRepository = studentRepository;
        this.opportunityRepository = opportunityRepository;
    }

    public List<SavedOpportunityDto> getSavedOpportunitiesByStudentId(Long studentId) {
        return savedOpportunityRepository.getSavedOpportunitiesByStudentId(studentId).stream().map(this::entityToDto).toList();
    }

    public SavedOpportunityDto saveOpportunity(CreateSavedOpportunityDto dto) {
        if (savedOpportunityRepository.isOpportunitySavedByStudent(dto.studentId(), dto.opportunityId())) {
            throw new IllegalArgumentException("This opportunity is already saved");
        }

        Student student = studentRepository.getStudentById(dto.studentId());
        ResearchOpportunity opportunity = opportunityRepository.getResearchOpportunity(dto.opportunityId());

        SavedOpportunity savedOpportunity = new SavedOpportunity();
        savedOpportunity.setStudent(student);
        savedOpportunity.setOpportunity(opportunity);

        return entityToDto(savedOpportunityRepository.saveOpportunity(savedOpportunity));
    }

    public void unsaveOpportunity(Long studentId, Long opportunityId) {
        savedOpportunityRepository.unsaveOpportunityByStudentIdAndOpportunityId(studentId, opportunityId);
    }

    private SavedOpportunityDto entityToDto(SavedOpportunity savedOpportunity) {
        return new SavedOpportunityDto(
            savedOpportunity.getId(),
            savedOpportunity.getStudent().getId(),
            savedOpportunity.getStudent().getName(),
            savedOpportunity.getOpportunity().getId(),
            savedOpportunity.getOpportunity().getTitle(),
            savedOpportunity.getSavedAt()
        );
    }
}
