package com.vsu.researchapp.presentation.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.dto.ApplicationDto;
import com.vsu.researchapp.application.dto.CreateApplicationDto;
import com.vsu.researchapp.application.dto.UpdateApplicationDto;
import com.vsu.researchapp.application.service.ApplicationService;
import com.vsu.researchapp.domain.model.Application.ApplicationStatus;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDto> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ApplicationDto>> getApplicationsByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(applicationService.getApplicationsByStudentId(studentId));
    }

    @GetMapping("/opportunity/{opportunityId}")
    public ResponseEntity<List<ApplicationDto>> getApplicationsByOpportunityId(
            @PathVariable Long opportunityId,
            @RequestParam(required = false) ApplicationStatus status) {

        if (status != null) {
            return ResponseEntity.ok(applicationService.getApplicationsByOpportunityIdAndStatus(opportunityId, status));
        }
        return ResponseEntity.ok(applicationService.getApplicationsByOpportunityId(opportunityId));
    }

    @PostMapping
    public ResponseEntity<ApplicationDto> applyToOpportunity(@Valid @RequestBody CreateApplicationDto dto) {
        ApplicationDto created = applicationService.applyToOpportunity(dto);
        return ResponseEntity.created(URI.create("/api/applications/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationDto> updateApplication(@PathVariable Long id, @RequestBody UpdateApplicationDto dto) {
        return ResponseEntity.ok(applicationService.updateApplication(dto, id));
    }

    // Faculty decision shortcut: PATCH /api/applications/5/status?status=ACCEPTED
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationDto> updateApplicationStatus(@PathVariable Long id, @RequestParam ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    // Sweeps accepted applications past their opportunity's end date and marks them COMPLETED
    @PostMapping("/complete-expired")
    public ResponseEntity<List<ApplicationDto>> completeExpiredAcceptedApplications() {
        return ResponseEntity.ok(applicationService.completeExpiredAcceptedApplications());
    }
}
