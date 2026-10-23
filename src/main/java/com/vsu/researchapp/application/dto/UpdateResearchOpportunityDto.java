package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateResearchOpportunityDto(
    String title,
    String description,
    String department,
    List<String> requiredMajors,
    List<String> requiredClassifications,
    List<String> requiredSkills,
    String availability,
    Float minimumGpa,

    @Schema(example = "2025-12-01", type = "string", format = "date")
    LocalDate applicationDeadline,

    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate startDate,

    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate,

    List<String> researchMediaUrls
) {}
