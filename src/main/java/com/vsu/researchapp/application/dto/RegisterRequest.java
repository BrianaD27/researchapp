package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    @NotBlank(message = "A username is required")
    String username,

    @NotBlank(message = "An email is required")
    @Email(message = "Must be a valid email address")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+edu$",
        flags = Pattern.Flag.CASE_INSENSITIVE,
        message = "Use a VSU email, or an email from one of VSU's partner schools")
    String email,

    @NotBlank(message = "A password is required")
    String password,

    String role
) {}
