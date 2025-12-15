package com.vsu.researchapp.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateResearchEventDto;
import com.vsu.researchapp.application.dto.ResearchEventDto;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchEvent;
import com.vsu.researchapp.domain.repository.ProfessorRepository;
import com.vsu.researchapp.domain.repository.ResearchEventRepository;

@Service
public class ResearchEventService {
    private final ResearchEventRepository researchEventRepository;

    private final ProfessorRepository professorRepository;

    public ResearchEventService(ResearchEventRepository researchEventRepository, ProfessorRepository professorRepository) {
        this.researchEventRepository = researchEventRepository;
        this.professorRepository = professorRepository;
    }

    public ResearchEventDto createResearchEvent(CreateResearchEventDto dto, Long professorId) {
        Professor professor = professorRepository.findById(professorId).orElseThrow(() -> new RuntimeException("Professor not found for id: " + professorId));

        ResearchEvent event = new ResearchEvent();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setAddress(dto.getAddress());
        event.setRegistrationLink(dto.getRegistrationLink());
        event.setBeginDate(dto.getBeginDate());
        event.setEndDate(dto.getEndDate());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setCreatedBy(professor);

        ResearchEvent saved = researchEventRepository.save(event);

        return entityToDto(saved);
    }  

    public List<ResearchEvent> getAllResearchEvents() {
        return researchEventRepository.findAll();
    }

    // Entity to DTO Method
    private ResearchEventDto entityToDto(ResearchEvent researchEvent) {
        ResearchEventDto dto = new ResearchEventDto();

        dto.setId(researchEvent.getId());
        dto.setTitle(researchEvent.getTitle());
        dto.setDescription(researchEvent.getDescription());
        dto.setAddress(researchEvent.getAddress());
        dto.setRegistrationLink(researchEvent.getRegistrationLink());
        dto.setBeginDate(researchEvent.getBeginDate());
        dto.setEndDate(researchEvent.getEndDate());
        dto.setStartTime(researchEvent.getStartTime());
        dto.setEndTime(researchEvent.getStartTime());
        dto.setCreatedAt(researchEvent.getCreatedAt());
        dto.setUpdatedAt(researchEvent.getUpdatedAt());
        dto.setCreatedBy(researchEvent.getCreatedBy().getId());
        return dto;
    }
}
