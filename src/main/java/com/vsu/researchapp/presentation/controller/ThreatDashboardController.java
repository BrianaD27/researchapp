package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.SecurityAuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/threats")
@CrossOrigin(origins = "*")
public class ThreatDashboardController {

    private final SecurityAuditService securityAuditService;

    public ThreatDashboardController(
            SecurityAuditService securityAuditService) {
        this.securityAuditService = securityAuditService;
    }

    // Full threat dashboard snapshot
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(
            securityAuditService.getSecurityDashboard());
    }

    // Recent failed logins
    @GetMapping("/failed-logins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFailedLogins() {
        return ResponseEntity.ok(
            securityAuditService.getRecentFailedLogins());
    }

    // International logins
    @GetMapping("/international")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getInternationalLogins() {
        return ResponseEntity.ok(
            securityAuditService.getInternationalLogins());
    }
}
