package com.vsu.researchapp.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.ConductReportService;
import com.vsu.researchapp.application.service.ConductReportService.ConductReportDto;

// /api/admin/** is ADMIN-only in SecurityConfig; the service checks again.
@RestController
@RequestMapping("/api/admin/conduct-reports")
public class ConductReportController {

    public record UpdateRequest(String status, String outcome, String adminNotes) {}

    private final ConductReportService service;

    public ConductReportController(ConductReportService service) {
        this.service = service;
    }

    @GetMapping
    public List<ConductReportDto> all() {
        return service.all();
    }

    @PutMapping("/{id}")
    public ConductReportDto update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        return service.update(id, request.status(), request.outcome(), request.adminNotes());
    }

    @PostMapping("/{id}/warn")
    public ConductReportDto warn(@PathVariable Long id) {
        return service.sendWarning(id);
    }

    @PostMapping("/{id}/suspend")
    public ConductReportDto suspend(@PathVariable Long id) {
        return service.suspend(id);
    }
}
