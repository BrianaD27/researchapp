package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.ResearchOpportunityService;
import com.vsu.researchapp.domain.model.ResearchOpportunity;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/research-opportunities")
@CrossOrigin(origins = "*")
public class ResearchOpportunityController {

    private final ResearchOpportunityService service;

    public ResearchOpportunityController(ResearchOpportunityService service) {
        this.service = service;
    }

    // LIST ALL
    @GetMapping
    public List<ResearchOpportunity> listAll() {
        return service.getAll();
    }

    // GET ONE BY ID
    @GetMapping("/{id}")
    public ResearchOpportunity getOne(@PathVariable Long id) {
        return service.getById(id);
    }

    // CREATE NEW
    @PostMapping
    public ResponseEntity<ResearchOpportunity> create(@RequestBody ResearchOpportunity opportunity) {
        ResearchOpportunity saved = service.create(opportunity);
        return ResponseEntity
                .created(URI.create("/api/research-opportunities/" + saved.getId()))
                .body(saved);
    }

    // UPDATE EXISTING
    @PutMapping("/{id}")
    public ResearchOpportunity update(@PathVariable Long id,
                                      @RequestBody ResearchOpportunity updated) {
        return service.update(id, updated);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
