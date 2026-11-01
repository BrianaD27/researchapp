package com.vsu.researchapp.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.vsu.researchapp.application.service.PartnerSchoolService;
import com.vsu.researchapp.application.service.PartnerSchoolService.MeDto;
import com.vsu.researchapp.application.service.PartnerSchoolService.PublicSchoolDto;
import com.vsu.researchapp.application.service.PartnerSchoolService.ResearchDto;
import com.vsu.researchapp.application.service.PartnerSchoolService.SchoolDto;

// Access is set in SecurityConfig: /api/partner/** is PARTNER or ADMIN,
// /api/admin/** is ADMIN, and the public school list sits under /api/v1/auth/**.
@RestController
public class PartnerSchoolController {

    public record AddRequest(String name, String emailDomain) {}
    public record ActiveRequest(boolean active) {}

    private final PartnerSchoolService service;

    public PartnerSchoolController(PartnerSchoolService service) {
        this.service = service;
    }

    // Names and domains only, so the sign-up page can say which schools are partners.
    @GetMapping("/api/v1/auth/partner-schools")
    public List<PublicSchoolDto> publicList() {
        return service.activePublic();
    }

    @GetMapping("/api/partner/me")
    public MeDto me() {
        return service.me();
    }

    @GetMapping("/api/partner/research")
    public List<ResearchDto> research() {
        return service.research();
    }

    @GetMapping("/api/admin/partner-schools")
    public List<SchoolDto> all() {
        return service.all();
    }

    @PostMapping("/api/admin/partner-schools")
    public SchoolDto add(@RequestBody AddRequest request) {
        return service.add(request.name(), request.emailDomain());
    }

    @PutMapping("/api/admin/partner-schools/{id}/active")
    public SchoolDto setActive(@PathVariable Long id, @RequestBody ActiveRequest request) {
        return service.setActive(id, request.active());
    }
}
