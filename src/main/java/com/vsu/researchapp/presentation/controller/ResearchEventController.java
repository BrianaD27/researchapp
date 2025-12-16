package com.vsu.researchapp.presentation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.dto.CreateResearchEventDto;
import com.vsu.researchapp.application.dto.ResearchEventDto;
import com.vsu.researchapp.application.dto.UpdateResearchEventDto;
import com.vsu.researchapp.application.service.ResearchEventService;
import com.vsu.researchapp.domain.model.Student;


import org.springframework.web.bind.annotation.RequestParam;

import com.vsu.researchapp.domain.model.ResearchEvent;


@RestController
@RequestMapping("/api/research-events")
@CrossOrigin(origins = "*")
public class ResearchEventController {
    private final ResearchEventService eventService;

    public ResearchEventController(ResearchEventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/{eventId}")
    public ResearchEventDto getResearchEventById(@PathVariable Long eventId) {
        return eventService.getResearchEventById(eventId);
    }

    @GetMapping
    public List<ResearchEventDto> getAllResearchEvents() {
        return eventService.getAllResearchEvents();
    }

    @GetMapping("/upcoming")
    public List<ResearchEventDto> getResearchEventsByUpcoming() {
        return eventService.getResearchEventsByUpcoming();
    }

    @GetMapping("/date-range")
    public List<ResearchEventDto> getResearchEventsByDateRange(@RequestParam LocalDate earliestDate, @RequestParam LocalDate latestDate) {
        return eventService.getResearchEventsByDateRange(earliestDate, latestDate);
    }

    @PostMapping
    public ResponseEntity<ResearchEventDto> createResearchEvent(@RequestBody CreateResearchEventDto dto, @RequestParam Long professorId) {
        ResearchEventDto event = eventService.createResearchEvent(dto, professorId);

        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @PutMapping("/{eventId}")
    public ResearchEventDto updateResearchEvent(@RequestBody UpdateResearchEventDto dto,  @PathVariable Long eventId) {
        return eventService.updateResearchEvent(dto, eventId);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteResearchEvent(  @PathVariable Long eventId) {
        eventService.deleteResearchEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}
