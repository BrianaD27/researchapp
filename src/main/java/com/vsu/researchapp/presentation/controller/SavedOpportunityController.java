package com.vsu.researchapp.presentation.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.dto.CreateSavedOpportunityDto;
import com.vsu.researchapp.application.dto.SavedOpportunityDto;
import com.vsu.researchapp.application.service.SavedOpportunityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/saved-opportunities")
public class SavedOpportunityController {

    private final SavedOpportunityService savedOpportunityService;

    public SavedOpportunityController(SavedOpportunityService savedOpportunityService) {
        this.savedOpportunityService = savedOpportunityService;
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<SavedOpportunityDto>> getSavedOpportunitiesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(savedOpportunityService.getSavedOpportunitiesByStudentId(studentId));
    }

    @PostMapping
    public ResponseEntity<SavedOpportunityDto> saveOpportunity(@Valid @RequestBody CreateSavedOpportunityDto dto) {
        SavedOpportunityDto saved = savedOpportunityService.saveOpportunity(dto);
        return ResponseEntity.created(URI.create("/api/saved-opportunities/" + saved.id())).body(saved);
    }

    @DeleteMapping("/student/{studentId}/opportunity/{opportunityId}")
    public ResponseEntity<Void> unsaveOpportunity(@PathVariable Long studentId, @PathVariable Long opportunityId) {
        savedOpportunityService.unsaveOpportunity(studentId, opportunityId);
        return ResponseEntity.noContent().build();
    }
}
