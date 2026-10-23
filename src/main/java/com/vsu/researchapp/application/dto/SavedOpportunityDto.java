package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record SavedOpportunityDto(
    Long id,
    Long studentId,
    String studentName,
    Long opportunityId,
    String opportunityTitle,
    LocalDateTime savedAt
) {}
