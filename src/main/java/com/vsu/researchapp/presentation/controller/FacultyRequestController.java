package com.vsu.researchapp.presentation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.FacultyRequestService;
import com.vsu.researchapp.application.service.FacultyRequestService.RequestView;

// Sign-up is public (under /api/v1/auth/**). The review routes are under
// /api/admin/** (admin only in SecurityConfig) and also annotated as a second lock.
@RestController
public class FacultyRequestController {

    public record FacultySignUpRequest(String username, String email, String password, String name, String department) {}

    private final FacultyRequestService service;

    public FacultyRequestController(FacultyRequestService service) {
        this.service = service;
    }

    @PostMapping("/api/v1/auth/register-faculty")
    public ResponseEntity<?> signUp(@RequestBody FacultySignUpRequest request) {
        service.signUp(request.username(), request.email(), request.password(), request.name(), request.department());
        return ResponseEntity.ok(Map.of("message",
            "Request sent. Check your VSU email for the activation link. An admin will approve your faculty account."));
    }

    @GetMapping("/api/admin/faculty-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public List<RequestView> pending() {
        return service.pending();
    }

    @PostMapping("/api/admin/faculty-requests/{userAccountId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approve(@PathVariable Long userAccountId) {
        service.approve(userAccountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/admin/faculty-requests/{userAccountId}/decline")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> decline(@PathVariable Long userAccountId) {
        service.decline(userAccountId);
        return ResponseEntity.noContent().build();
    }
}
