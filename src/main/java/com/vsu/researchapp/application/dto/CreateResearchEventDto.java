package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateResearchEventDto(
    String title,
    String description,
    String address,
    String registrationLink,

    @NotBlank(message = "A begin date is required")
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate beginDate,
    
    @NotBlank(message = "An end date is required")
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate,
    
    @NotBlank(message = "A start time is required")
    @Schema(example = "09:00:00", type = "string", format = "time")
    LocalTime startTime,
    
    @NotBlank(message = "An end time is required")
    @Schema(example = "17:00:00", type = "string", format = "time")
    LocalTime endTime
) {}
