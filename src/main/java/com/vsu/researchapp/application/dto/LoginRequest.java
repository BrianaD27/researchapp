package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "A username is required")
    String username,

    @NotBlank(message = "A password is required")
    String password
) {}
