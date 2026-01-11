package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResearchOpportunityDto(
    Long id,
    String title,
    String description,
    String requirements,
    LocalDate beginDate,
    LocalDate endDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long createdById,
    String createdByName
) {}
