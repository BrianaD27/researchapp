package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.ProfessorService;
import com.vsu.researchapp.domain.model.Professor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
@CrossOrigin(origins = "*")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }

    @GetMapping
    public List<Professor> getAllProfessors() {
        return professorService.getAllProfessors();
    }

    @GetMapping("/{id}")
    public Professor getProfessor(@PathVariable Long id) {
        return professorService.getProfessorById(id);
    }

    @PostMapping
    public Professor createProfessor(@RequestBody Professor professor) {
        return professorService.createProfessor(professor);
    }

    @PutMapping("/{id}")
    public Professor updateProfessor(@PathVariable Long id,
                                     @RequestBody Professor professor) {
        return professorService.updateProfessor(id, professor);
    }

    @DeleteMapping("/{id}")
    public void deleteProfessor(@PathVariable Long id) {
        professorService.deleteProfessor(id);
    }
}
