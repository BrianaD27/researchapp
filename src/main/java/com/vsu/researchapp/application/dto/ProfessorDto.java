package com.vsu.researchapp.application.dto;

public record ProfessorDto (
    Long id,
    String name,
    String email,
    String department,
    String title
) {}