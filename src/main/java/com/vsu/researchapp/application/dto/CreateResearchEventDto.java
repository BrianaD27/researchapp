package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateResearchEventDto(
    String title,
    String description,
    String address,
    String registrationLink,
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate beginDate,
    
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate,
    
    @Schema(example = "09:00:00", type = "string", format = "time")
    LocalTime startTime,
    
    @Schema(example = "17:00:00", type = "string", format = "time")
    LocalTime endTime
) {}
