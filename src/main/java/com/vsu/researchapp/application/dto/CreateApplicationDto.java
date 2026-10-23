package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotNull;

public record CreateApplicationDto(
    @NotNull(message = "A student id is required")
    Long studentId,

    @NotNull(message = "A research opportunity id is required")
    Long researchOpportunityId
) {}
