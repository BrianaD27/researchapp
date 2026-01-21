package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStudentDto(
    @NotBlank(message = "A name is required")
    String name,

    @NotBlank(message = "An email is required")
    String email,

    @NotBlank(message = "A graduate year is required")
    Integer graduateYear,

    @NotBlank(message = "A major is required")
    String major,

    @NotBlank(message = "A description is required")
    String description,
    
    @NotBlank(message = "Skills are required")
    String skills
) {}