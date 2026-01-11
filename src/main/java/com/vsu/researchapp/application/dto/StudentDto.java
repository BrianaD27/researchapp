package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record StudentDto(
    Long id,
    String name,
    String email,
    String major,
    String description,
    String skills,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
