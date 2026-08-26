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

import com.vsu.researchapp.application.dto.CreateSavedStudentDto;
import com.vsu.researchapp.application.dto.SavedStudentDto;
import com.vsu.researchapp.application.service.SavedStudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/saved-students")
public class SavedStudentController {

    private final SavedStudentService savedStudentService;

    public SavedStudentController(SavedStudentService savedStudentService) {
        this.savedStudentService = savedStudentService;
    }

    @GetMapping("/professor/{professorId}")
    public ResponseEntity<List<SavedStudentDto>> getSavedStudentsByProfessorId(@PathVariable Long professorId) {
        return ResponseEntity.ok(savedStudentService.getSavedStudentsByProfessorId(professorId));
    }

    @PostMapping
    public ResponseEntity<SavedStudentDto> saveStudent(@Valid @RequestBody CreateSavedStudentDto dto) {
        SavedStudentDto saved = savedStudentService.saveStudent(dto);
        return ResponseEntity.created(URI.create("/api/saved-students/" + saved.id())).body(saved);
    }

    @DeleteMapping("/professor/{professorId}/student/{studentId}")
    public ResponseEntity<Void> unsaveStudent(@PathVariable Long professorId, @PathVariable Long studentId) {
        savedStudentService.unsaveStudent(professorId, studentId);
        return ResponseEntity.noContent().build();
    }
}
