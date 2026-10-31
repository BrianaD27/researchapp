package com.vsu.researchapp.application.dto;

import java.util.List;

public record UpdateStudentDto(
    String name,
    String major,
    Integer graduationYear,
    String classification,
    String description,
    String previousExperience,
    @jakarta.validation.constraints.Size(max = 1000, message = "Outside interests must be under 1000 characters")
    String outsideInterests,
    Float gpa,
    Integer availableHoursPerWeek,
    String resumeUrl,
    List<String> skills
) {}
