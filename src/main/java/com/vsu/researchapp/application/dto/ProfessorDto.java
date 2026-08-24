package com.vsu.researchapp.application.dto;
import java.time.LocalDateTime;

public record ProfessorDto(
    Long id,
    String name,
    String email,
    String department,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}