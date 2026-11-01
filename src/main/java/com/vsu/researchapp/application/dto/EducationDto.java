package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EducationDto(
    @NotBlank(message = "Each degree needs a name")
    @Size(max = 150, message = "Degree must be under 150 characters")
    String degree,

    @NotBlank(message = "Each degree needs a school")
    @Size(max = 150, message = "School must be under 150 characters")
    String school,

    @Size(max = 20, message = "Year must be under 20 characters")
    String graduationYear,

    Boolean inProgress
) {}
