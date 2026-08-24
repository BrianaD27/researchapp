package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSavedStudentDto(
    @NotNull(message = "A professor id is required")
    Long professorId,

    @NotNull(message = "A student id is required")
    Long studentId
) {}
