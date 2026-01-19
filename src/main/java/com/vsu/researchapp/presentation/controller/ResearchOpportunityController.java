package com.vsu.researchapp.presentation.controller;
import com.vsu.researchapp.application.service.ResearchOpportunityService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import com.vsu.researchapp.application.dto.CreateResearchOpportunityDto;
import com.vsu.researchapp.application.dto.ResearchOpportunityDto;
import com.vsu.researchapp.application.dto.UpdateResearchOpportunityDto;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/research-opportunities")
@CrossOrigin(origins = "*")
public class ResearchOpportunityController {

    private final ResearchOpportunityService service;

    public ResearchOpportunityController(ResearchOpportunityService service) {
        this.service = service;
    }

    @GetMapping()
    public ResponseEntity<List<ResearchOpportunityDto>> getAll() {
        List<ResearchOpportunityDto> opportunities = service.getAllResearchOpportunities();
        return ResponseEntity.ok(opportunities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResearchOpportunityDto> getById(@Valid @PathVariable Long id) {
        ResearchOpportunityDto opportunity = service.getResearchOpportunityById(id);
        return ResponseEntity.ok(opportunity);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ResearchOpportunityDto>> getByUpcoming() {
        List<ResearchOpportunityDto> opportunities = service.getResearchOpportunitiesByUpcoming();
        return ResponseEntity.ok(opportunities);
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<ResearchOpportunityDto>> getByDateRange(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate earliestDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate latestDate) {

        return ResponseEntity.ok(service.getResearchOpportunitiesByDateRange(earliestDate, latestDate));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResearchOpportunityDto>> search(@RequestParam String term) {
        List<ResearchOpportunityDto> opportunities = service.search(term);
        return ResponseEntity.ok(opportunities);
    }

    @PostMapping
    public ResponseEntity<ResearchOpportunityDto> createResearchOpportunity(@Valid @RequestBody CreateResearchOpportunityDto dto, @RequestParam Long professorId) {
        ResearchOpportunityDto created = service.createResearchOpportunity(dto, professorId);

        return ResponseEntity.created(URI.create("/api/research-opportunities/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResearchOpportunityDto> updateResearchOpportunity(@PathVariable Long id, @Valid @RequestBody UpdateResearchOpportunityDto dto) {
        ResearchOpportunityDto updated = service.updateResearchOpportunity(dto, id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResearchOpportunity(@PathVariable Long id) {
        service.deleteResearchOpportunity(id);
        return ResponseEntity.noContent().build();
    }
}
