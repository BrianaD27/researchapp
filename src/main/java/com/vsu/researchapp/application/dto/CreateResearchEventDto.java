package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record CreateResearchEventDto(

    @NotBlank(message = "A title is required")
    @Size(min = 2, max = 200, message = "Title must be between 2 and 200 characters")
    String title,

    @NotBlank(message = "A description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    String description,

    @NotBlank(message = "An address is required")
    @Size(max = 300, message = "Address must be under 300 characters")
    String address,

    @Size(max = 500, message = "Registration link must be under 500 characters")
    String registrationLink,

    @NotNull(message = "A begin date is required")
    @Schema(example = "2025-12-25", type = "string", format = "date")
    LocalDate beginDate,

    @NotNull(message = "An end date is required")
    @Schema(example = "2025-12-26", type = "string", format = "date")
    LocalDate endDate,

    @NotNull(message = "A start time is required")
    @Schema(example = "09:00:00", type = "string", format = "time")
    LocalTime startTime,

    @NotNull(message = "An end time is required")
    @Schema(example = "17:00:00", type = "string", format = "time")
    LocalTime endTime

) {}
