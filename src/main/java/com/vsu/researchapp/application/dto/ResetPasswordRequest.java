package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
    @NotBlank(message = "A reset token is required")
    String token,

    @NotBlank(message = "A new password is required")
    String newPassword
) {}
