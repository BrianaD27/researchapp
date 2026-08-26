package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record Verify2FARequest(
    @NotBlank(message = "A username is required")
    String username,

    @NotBlank(message = "A verification code is required")
    String code
) {}
