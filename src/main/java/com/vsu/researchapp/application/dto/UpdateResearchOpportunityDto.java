package com.vsu.researchapp.application.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

public record  UpdateResearchOpportunityDto(
    String title,
    String description,
    String requirements,

    
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate beginDate,
    
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate
) {}
