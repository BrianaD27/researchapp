package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "A username is required")
    String username,

    @NotBlank(message = "An email is required")
    @Email(message = "Must be a valid email address")
    String email,

    @NotBlank(message = "A password is required")
    String password,

    String role
) {}
