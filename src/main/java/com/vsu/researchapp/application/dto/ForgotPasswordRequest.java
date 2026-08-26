package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "A username is required")
    String username
) {}
