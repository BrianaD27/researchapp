package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "A username is required")
    String username
) {}
