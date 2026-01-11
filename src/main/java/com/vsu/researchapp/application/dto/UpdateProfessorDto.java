package com.vsu.researchapp.application.dto;

public record UpdateProfessorDto(
    String name,
    String email,
    String department,
    String title
) {}