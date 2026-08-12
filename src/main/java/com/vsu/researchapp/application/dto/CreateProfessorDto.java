package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.*;

public record CreateProfessorDto(

    @NotBlank(message = "A name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Name contains invalid characters")
    String name,

    @NotBlank(message = "A faculty email is required")
    @Email(message = "Must be a valid email address")
    @Size(max = 255, message = "Email too long")
    String email,

    @NotBlank(message = "A department is required")
    @Size(min = 2, max = 100, message = "Department must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s,&()-]+$", message = "Department contains invalid characters")
    String department,

    @NotBlank(message = "A title is required")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s,&().'-]+$", message = "Title contains invalid characters")
    String title

) {}
