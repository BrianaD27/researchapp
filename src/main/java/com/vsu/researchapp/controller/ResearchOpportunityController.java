package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.service.ResearchOpportunityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/opportunities")
@CrossOrigin(origins = "*")
public class ResearchOpportunityController {

    private final ResearchOpportunityService opportunityService;

    public ResearchOpportunityController(ResearchOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public List<ResearchOpportunity> getAllOpportunities() {
        return opportunityService.getAllOpportunities();
    }

    @GetMapping("/{id}")
    public ResearchOpportunity getOpportunity(@PathVariable Long id) {
        return opportunityService.getOpportunityById(id);
    }

    @PostMapping
    public ResearchOpportunity createOpportunity(@RequestBody ResearchOpportunity opportunity) {
        return opportunityService.createOpportunity(opportunity);
    }

    @PutMapping("/{id}")
    public ResearchOpportunity updateOpportunity(@PathVariable Long id,
                                                 @RequestBody ResearchOpportunity opportunity) {
        return opportunityService.updateOpportunity(id, opportunity);
    }

    @DeleteMapping("/{id}")
    public void deleteOpportunity(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
    }
}
