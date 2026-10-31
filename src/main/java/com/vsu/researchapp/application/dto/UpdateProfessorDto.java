package com.vsu.researchapp.application.dto;

public record UpdateProfessorDto(
    String name,
    String department,
    String officeLocation,
    String description,
    String profilePictureUrl,

    @jakarta.validation.constraints.Size(max = 1000, message = "Outside interests must be under 1000 characters")
    String outsideInterests,

    @jakarta.validation.constraints.Size(max = 10, message = "List up to 10 degrees")
    java.util.List<@jakarta.validation.Valid EducationDto> education
) {}
