package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    // Username or the account's email address.
    @NotBlank(message = "A username or email is required")
    String username
) {}
