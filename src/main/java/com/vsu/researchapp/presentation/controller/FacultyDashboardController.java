package com.vsu.researchapp.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.FacultyDashboardService;
import com.vsu.researchapp.application.service.FacultyDashboardService.Dashboard;

// Faculty "My Dashboard". The service decides what each role may see.
@RestController
public class FacultyDashboardController {

    private final FacultyDashboardService service;

    public FacultyDashboardController(FacultyDashboardService service) {
        this.service = service;
    }

    @GetMapping("/api/faculty-dashboard")
    public Dashboard mine(@RequestHeader(value = "X-Acting-For", required = false) String actingFor) {
        return service.mine(actingFor);
    }
}
