package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repository.ProfessorRepository;
import com.vsu.researchapp.domain.repository.ResearchOpportunityRepository;
import com.vsu.researchapp.domain.repository.StudentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final ResearchOpportunityRepository opportunityRepository;

    public SearchController(
            StudentRepository studentRepository,
            ProfessorRepository professorRepository,
            ResearchOpportunityRepository opportunityRepository) {
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.opportunityRepository = opportunityRepository;
    }

    // Search everything at once
    @GetMapping
    public ResponseEntity<Map<String, Object>> searchAll(
            @RequestParam String q) {

        Map<String, Object> results = new HashMap<>();

        results.put("students",
            studentRepository.searchStudents(q));
        results.put("professors",
            professorRepository.searchProfessors(q));
        results.put("opportunities",
            opportunityRepository.searchOpportunities(q));
        results.put("query", q);

        return ResponseEntity.ok(results);
    }

    // Search students only
    @GetMapping("/students")
    public ResponseEntity<List<Student>> searchStudents(
            @RequestParam String q) {
        return ResponseEntity.ok(
            studentRepository.searchStudents(q));
    }

    // Search professors only
    @GetMapping("/professors")
    public ResponseEntity<List<Professor>> searchProfessors(
            @RequestParam String q) {
        return ResponseEntity.ok(
            professorRepository.searchProfessors(q));
    }

    // Search professors by department
    @GetMapping("/professors/department")
    public ResponseEntity<List<Professor>> searchByDepartment(
            @RequestParam String department) {
        return ResponseEntity.ok(
            professorRepository.findByDepartmentIgnoreCase(department));
    }

    // Search research opportunities only
    @GetMapping("/opportunities")
    public ResponseEntity<List<ResearchOpportunity>> searchOpportunities(
            @RequestParam String q) {
        return ResponseEntity.ok(
            opportunityRepository.searchOpportunities(q));
    }

    // Get upcoming opportunities
    @GetMapping("/opportunities/upcoming")
    public ResponseEntity<List<ResearchOpportunity>> getUpcoming() {
        return ResponseEntity.ok(
            opportunityRepository.getResearchOpportunitiesByUpcoming());
    }
}
