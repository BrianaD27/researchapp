package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.StudentApplicationService;
import com.vsu.researchapp.application.service.AdminAuditLogService;
import com.vsu.researchapp.domain.model.StudentApplication;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/applications")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // <-- locks down everything in this controller
public class StudentApplicationAdminController {

    private final StudentApplicationService service;
    private final AdminAuditLogService audit;

    public StudentApplicationAdminController(StudentApplicationService service, AdminAuditLogService audit) {
        this.service = service;
        this.audit = audit;
    }

    @GetMapping
    public List<StudentApplication> all() {
        audit.log("ADMIN_VIEW_ALL_APPLICATIONS");
        return service.getAllApplications();
    }

    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<StudentApplication> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateStatusRequest body
    ) {
        String status = body.status == null ? "" : body.status.trim().toUpperCase();
        if (!(status.equals("PENDING") || status.equals("ACCEPTED") || status.equals("REJECTED"))) {
            return ResponseEntity.badRequest().build();
        }

        audit.log("ADMIN_UPDATE_APPLICATION_STATUS id=" + applicationId + " status=" + status);
        return ResponseEntity.ok(service.updateStatus(applicationId, status));
    }

    public static class UpdateStatusRequest {
        public String status;
    }
}
