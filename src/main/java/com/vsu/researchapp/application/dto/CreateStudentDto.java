package com.vsu.researchapp.application.dto;

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

    @NotNull(message = "A graduate year is required")
    @Min(value = 2000, message = "Graduate year must be 2000 or later")
    @Max(value = 2100, message = "Graduate year is not valid")
    Integer graduateYear,

    @NotBlank(message = "A major is required")
    @Size(min = 2, max = 100, message = "Major must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s,&()-]+$", message = "Major contains invalid characters")
    String major,

    @NotBlank(message = "A description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    String description,

    @NotBlank(message = "Skills are required")
    @Size(max = 500, message = "Skills must be under 500 characters")
    String skills

) {}
