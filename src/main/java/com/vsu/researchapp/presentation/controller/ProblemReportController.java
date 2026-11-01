package com.vsu.researchapp.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.dto.ProblemReportDto;
import com.vsu.researchapp.application.service.ProblemReportService;

@RestController
@RequestMapping("/api/reports")
public class ProblemReportController {

    public record SubmitRequest(String category, String description, String page) {}
    public record UpdateRequest(String status, String reply) {}

    private final ProblemReportService service;

    public ProblemReportController(ProblemReportService service) {
        this.service = service;
    }

    @PostMapping
    public ProblemReportDto submit(@RequestBody SubmitRequest request,
            @RequestHeader(value = "User-Agent", required = false) String browser) {
        return service.submit(request.category(), request.description(), request.page(), browser);
    }

    @GetMapping("/mine")
    public List<ProblemReportDto> mine() {
        return service.mine();
    }

    // Admin only (checked in the service).
    @GetMapping
    public List<ProblemReportDto> all() {
        return service.all();
    }

    @PutMapping("/{id}")
    public ProblemReportDto update(@PathVariable Long id, @RequestBody UpdateRequest request) {
        return service.update(id, request.status(), request.reply());
    }
}
