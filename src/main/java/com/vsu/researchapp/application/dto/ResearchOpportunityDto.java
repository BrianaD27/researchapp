package com.vsu.researchapp.application.dto;

public record ResearchOpportunityDto(
    Long id,
    String title,
    String description,
    String field
) {}
