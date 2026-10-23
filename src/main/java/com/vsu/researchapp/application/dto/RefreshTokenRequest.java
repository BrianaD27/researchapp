package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "A refresh token is required")
    String refreshToken
) {}
