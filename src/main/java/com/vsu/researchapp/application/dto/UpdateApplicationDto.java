package com.vsu.researchapp.application.dto;

import com.vsu.researchapp.domain.model.Application.ApplicationStatus;
import com.vsu.researchapp.domain.model.Application.OpportunityStatus;

// Used for both the student-side bookmark -> applied transition and the
// faculty-side pending/accepted/rejected decision, so both fields are optional.
public record UpdateApplicationDto(
    OpportunityStatus opportunityStatus,
    ApplicationStatus applicationStatus
) {}
