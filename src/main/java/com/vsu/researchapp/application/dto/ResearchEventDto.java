package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ResearchEventDto(
    Long id,
    String title,
    String description,
    String address,
    String registrationLink,
    LocalDate beginDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long createdById,
    String createdByName
) {}