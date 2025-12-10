
package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.model.StudentApplication;
import com.vsu.researchapp.service.StudentApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class StudentApplicationController {

    private final StudentApplicationService service;

    public StudentApplicationController(StudentApplicationService service) {
        this.service = service;
    }

    // STUDENT: apply to an opportunity
    // POST /api/opportunities/{opportunityId}/apply
    @PostMapping("/opportunities/{opportunityId}/apply")
    public ResponseEntity<StudentApplication> apply(
            @PathVariable Long opportunityId,
            @RequestBody StudentApplication request) {

        StudentApplication saved = service.applyToOpportunity(opportunityId, request);
        return ResponseEntity.ok(saved);
    }

    // PROFESSOR: view applications for an opportunity
    // GET /api/opportunities/{opportunityId}/applications
    @GetMapping("/opportunities/{opportunityId}/applications")
    public List<StudentApplication> getForOpportunity(@PathVariable Long opportunityId) {
        return service.getApplicationsForOpportunity(opportunityId);
    }

    // GET one application by id
    // GET /api/applications/{id}
    @GetMapping("/applications/{id}")
    public StudentApplication getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    // PROFESSOR: update status (ACCEPTED / REJECTED / PENDING)
    // PUT /api/applications/{id}/status?value=ACCEPTED
    @PutMapping("/applications/{id}/status")
    public StudentApplication updateStatus(@PathVariable Long id,
                                           @RequestParam("value") String status) {
        return service.updateStatus(id, status);
    }
}