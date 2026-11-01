package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record ProblemReportDto(
    Long id,
    String category,
    String description,
    String page,
    String browser,
    String reporterUsername,
    String status,
    String adminReply,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
