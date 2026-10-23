package com.vsu.researchapp.application.dto;

import java.util.List;

public record UpdateStudentDto(
    String name,
    String major,
    Integer graduationYear,
    String classification,
    String description,
    String previousExperience,
    Float gpa,
    Integer availableHoursPerWeek,
    String resumeUrl,
    List<String> skills
) {}
