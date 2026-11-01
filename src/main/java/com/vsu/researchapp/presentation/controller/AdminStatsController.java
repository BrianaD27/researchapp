package com.vsu.researchapp.presentation.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.AdminStatsService;

// /api/admin/** is already admin-only in SecurityConfig; the annotation is a second lock.
@RestController
public class AdminStatsController {

    private final AdminStatsService service;

    public AdminStatsController(AdminStatsService service) {
        this.service = service;
    }

    @GetMapping("/api/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminStatsService.Stats stats() {
        return service.stats();
    }
}
