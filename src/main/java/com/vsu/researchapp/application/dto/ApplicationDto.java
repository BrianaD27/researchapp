package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

import com.vsu.researchapp.domain.model.Application.ApplicationStatus;
import com.vsu.researchapp.domain.model.Application.OpportunityStatus;

public record ApplicationDto(
    Long id,
    Long studentId,
    String studentName,
    Long researchOpportunityId,
    String researchOpportunityTitle,
    OpportunityStatus opportunityStatus,
    ApplicationStatus applicationStatus,
    LocalDateTime appliedAt,
    LocalDateTime updatedAt
) {}
