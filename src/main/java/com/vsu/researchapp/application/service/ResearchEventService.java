package com.vsu.researchapp.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.application.dto.CreateResearchEventDto;
import com.vsu.researchapp.application.dto.ResearchEventDto;
import com.vsu.researchapp.application.dto.UpdateResearchEventDto;
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


    public ResearchEvent getResearchEventById(Long eventId) {
        return researchEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("There is no Research event with the id: " + eventId));
    }

    public List<ResearchEvent> getAllResearchEvents() {
        return researchEventRepository.findAll();
    }

    public List<ResearchEventDto> getResearchEventsByUpcoming() {
        List<ResearchEvent> events = researchEventRepository.getResearchEventsByUpcoming();

        return events.stream().map(this::entityToDto).toList();
    }

    public List<ResearchEventDto> getResearchEventsByDateRange(LocalDate earliestDate, LocalDate latestDate) {
        List<ResearchEvent> events = researchEventRepository.getResearchEventsByDateRange(earliestDate, latestDate);

        return events.stream().map(this::entityToDto).toList();

    }

    public ResearchEventDto createResearchEvent(CreateResearchEventDto dto, Long professorId) {
        Professor professor = professorRepository.findById(professorId).orElseThrow(() -> new RuntimeException("Professor not found for id: " + professorId));

        //Validate Date
        if (dto.beginDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("The beginning date must be before the end date");
        }
        if (dto.beginDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The beginning date must be after the today's date: " + LocalDate.now());
        }

        //Validate Time
        if (dto.startTime().isAfter(dto.endTime())) {
            throw new IllegalArgumentException("The start time must be before the end time");
        }

        ResearchEvent event = new ResearchEvent();
        event.setTitle(dto.title());
        event.setDescription(dto.description());
        event.setAddress(dto.address());
        event.setRegistrationLink(dto.registrationLink());
        event.setBeginDate(dto.beginDate());
        event.setEndDate(dto.endDate());
        event.setStartTime(dto.startTime());
        event.setEndTime(dto.endTime());
        event.setCreatedBy(professor);

        ResearchEvent saved = researchEventRepository.save(event);

        return entityToDto(saved);
    }
    
    public ResearchEventDto updateResearchEvent(UpdateResearchEventDto dto, Long eventId) {
        ResearchEvent event = researchEventRepository.findById(eventId).orElseThrow(() -> new RuntimeException("Event not found for id: " + eventId));

        //Validate Date
        if (dto.beginDate().isAfter(dto.endDate())) {
            throw new IllegalArgumentException("The beginning date must be before the end date");
        }
        if (dto.beginDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The beginning date must be after the today's date: " + LocalDate.now());
        }

        //Validate Time
        if (dto.startTime().isAfter(dto.endTime())) {
            throw new IllegalArgumentException("The start time must be before the end time");
        }

        Optional.ofNullable(dto.title()).ifPresent(event::setTitle);
        Optional.ofNullable(dto.description()).ifPresent(event::setDescription);
        Optional.ofNullable(dto.address()).ifPresent(event::setAddress);
        Optional.ofNullable(dto.registrationLink()).ifPresent(event::setRegistrationLink);
        Optional.ofNullable(dto.beginDate()).ifPresent(event::setBeginDate);
        Optional.ofNullable(dto.endDate()).ifPresent(event::setEndDate);
        Optional.ofNullable(dto.startTime()).ifPresent(event::setStartTime);
        Optional.ofNullable(dto.endTime()).ifPresent(event::setEndTime);

        ResearchEvent savedUpdatedEvent = researchEventRepository.save(event);

        return entityToDto(savedUpdatedEvent);
    }

    public void deleteResearchEvent(Long eventId) {
        if (!researchEventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found for id: " + eventId);
        }

        researchEventRepository.deleteById(eventId);
    }

    // Entity to DTO Method
    private ResearchEventDto entityToDto(ResearchEvent event) {
        return new ResearchEventDto(
            event.getId(),
            event.getTitle(),
            event.getDescription(),
            event.getAddress(),
            event.getRegistrationLink(),
            event.getBeginDate(),
            event.getEndDate(),
            event.getStartTime(),
            event.getEndTime(),
            event.getCreatedAt(),
            event.getUpdatedAt(),
            event.getCreatedBy().getId(),
            event.getCreatedBy().getName()
        );
    }
}
