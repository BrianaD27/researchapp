package com.vsu.researchapp.application.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfessorDto (
    Long id,

    @NotBlank(message = "A name is required")
    String name,

    @NotBlank(message = "A faculty email is required")
    @Email(message = "Must be in an Email format")
    String email,

    @NotBlank(message = "A department is required")
    String department,

    @NotBlank(message = "A title is required")
    String title
) {}