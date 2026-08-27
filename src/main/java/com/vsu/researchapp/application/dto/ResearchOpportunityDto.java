package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ResearchOpportunityDto(
    Long id,
    String title,
    String description,
    String department,
    List<String> requiredMajors,
    List<String> requiredClassifications,
    List<String> requiredSkills,
    String availability,
    Float minimumGpa,
    LocalDate applicationDeadline,
    LocalDate startDate,
    LocalDate endDate,
    List<String> researchMediaUrls,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long professorId,
    String professorName
) {}
