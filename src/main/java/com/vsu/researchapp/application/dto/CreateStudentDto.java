package com.vsu.researchapp.application.dto;

import java.util.List;

import jakarta.validation.constraints.*;

public record CreateStudentDto(

    @NotBlank(message = "A name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name contains invalid characters")
    String name,

    @NotBlank(message = "An email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email too long")
    String email,

    @NotBlank(message = "A major is required")
    @Size(min = 2, max = 100, message = "Major must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s,&()-]+$", message = "Major contains invalid characters")
    String major,

    @NotNull(message = "A graduation year is required")
    @Min(value = 2000, message = "Graduation year must be 2000 or later")
    @Max(value = 2100, message = "Graduation year is not valid")
    Integer graduationYear,

    @NotBlank(message = "A classification is required")
    @Size(max = 50, message = "Classification must be under 50 characters")
    String classification,

    @NotBlank(message = "A description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    String description,

    @Size(max = 1000, message = "Previous experience must be under 1000 characters")
    String previousExperience,

    @DecimalMin(value = "0.0", message = "GPA must be 0.0 or higher")
    @DecimalMax(value = "4.0", message = "GPA must be 4.0 or lower")
    Float gpa,

    @Min(value = 0, message = "Available hours per week cannot be negative")
    @Max(value = 80, message = "Available hours per week is not valid")
    Integer availableHoursPerWeek,

    @NotEmpty(message = "At least one skill is required")
    List<String> skills

) {}
