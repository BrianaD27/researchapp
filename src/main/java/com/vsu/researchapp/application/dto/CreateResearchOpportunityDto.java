package com.vsu.researchapp.application.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateResearchOpportunityDto(
    @NotBlank(message = "A title is required")
    String title,

    @NotBlank(message = "A description is required")
    String description,

    @NotBlank(message = "Requirements are required")
    String requirements,

    @NotBlank(message = "A begin date is required")
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate beginDate,
    
    @NotBlank(message = "An end date is required")
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate
) {}