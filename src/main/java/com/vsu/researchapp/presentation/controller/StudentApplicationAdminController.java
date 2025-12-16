package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.model.StudentApplication;
import com.vsu.researchapp.service.StudentApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
@CrossOrigin(origins = "*")
public class StudentApplicationAdminController {

    private final StudentApplicationService service;

    public StudentApplicationAdminController(StudentApplicationService service) {
        this.service = service;
    }

    // 1) Admin: get ALL applications in the system
    @GetMapping
    public List<StudentApplication> getAllApplications() {
        return service.getAllApplications();
    }

    // 2) Admin: update the status of ONE application
    // Example: PATCH /api/admin/applications/5/status?status=ACCEPTED
    @PatchMapping("/{applicationId}/status")
    public StudentApplication updateStatus(
            @PathVariable Long applicationId,
            @RequestParam String status) {

        return service.updateStatus(applicationId, status);
    }
}
