package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record SavedStudentDto(
    Long id,
    Long professorId,
    String professorName,
    Long studentId,
    String studentName,
    LocalDateTime savedAt,
    LocalDateTime updatedAt
) {}
