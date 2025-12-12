package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.StudentApplicationService;
import com.vsu.researchapp.domain.model.StudentApplication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities/{opportunityId}/applications")
@CrossOrigin(origins = "*")
public class StudentApplicationController {

    private final StudentApplicationService service;

    public StudentApplicationController(StudentApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public StudentApplication create(@PathVariable Long opportunityId,
                                     @RequestBody StudentApplication input) {
        return service.applyToOpportunity(opportunityId, input);
    }

    @GetMapping
    public List<StudentApplication> list(@PathVariable Long opportunityId) {
        return service.getApplicationsByOpportunity(opportunityId);
    }
}
