package com.vsu.researchapp.presentation.controller;

import java.net.URI;
import java.util.List;

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

import com.vsu.researchapp.application.dto.CreateProfessorDto;
import com.vsu.researchapp.application.dto.ProfessorDto;
import com.vsu.researchapp.application.dto.UpdateProfessorDto;
import com.vsu.researchapp.application.service.ProfessorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/professors")
@CrossOrigin(origins = "*")
public class ProfessorController {

    private final ProfessorService professorService;
    private static final String BASE_URL = "/api/professors/";

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping()
    public ResponseEntity<List<ProfessorDto>> getAllProfessors() {
        return ResponseEntity.ok(professorService.getAllProfessors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfessorDto> getProfessorById(@PathVariable Long id) {
        return ResponseEntity.ok(professorService.getProfessorById(id));
    }

    @PostMapping
    public ResponseEntity<ProfessorDto> createProfessor(@Valid @RequestBody CreateProfessorDto dto) {
        ProfessorDto created = professorService.createProfessor(dto);

        // Returns url location of newly created Professor
        return ResponseEntity.created(URI.create(BASE_URL + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfessorDto> updateProfessor(@PathVariable Long id, @Valid @RequestBody UpdateProfessorDto updated) {
        return ResponseEntity.ok(professorService.updateProfessor(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@Valid @PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
    
}
