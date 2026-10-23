package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateSavedOpportunityDto(
    @NotNull(message = "A student id is required")
    Long studentId,

    @NotNull(message = "An opportunity id is required")
    Long opportunityId
) {}
