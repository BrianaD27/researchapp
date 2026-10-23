package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateResearchOpportunityDto(
    @NotBlank(message = "A title is required")
    String title,

    @NotBlank(message = "A description is required")
    String description,

    @NotBlank(message = "A department is required")
    String department,

    @NotEmpty(message = "At least one required major is needed")
    List<String> requiredMajors,

    List<String> requiredClassifications,

    List<String> requiredSkills,

    @NotBlank(message = "Availability is required")
    String availability,

    @DecimalMin(value = "0.0", message = "Minimum GPA must be 0.0 or higher")
    @DecimalMax(value = "4.0", message = "Minimum GPA must be 4.0 or lower")
    Float minimumGpa,

    @NotNull(message = "An application deadline is required")
    @Schema(example = "2025-12-01", type = "string", format = "date")
    LocalDate applicationDeadline,

    @NotNull(message = "A start date is required")
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate startDate,

    @NotNull(message = "An end date is required")
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate,

    List<String> researchMediaUrls
) {}
