package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;
import java.util.List;

public record StudentDto(
    Long id,
    String name,
    String email,
    String major,
    Integer graduationYear,
    String classification,
    String description,
    String previousExperience,
    Float gpa,
    Integer availableHoursPerWeek,
    String resumeUrl,
    String profilePictureUrl,
    List<String> skills,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

