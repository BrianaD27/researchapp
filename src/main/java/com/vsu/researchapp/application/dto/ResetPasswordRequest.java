package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank @Size(max = 500) String token,
    @NotBlank @Size(min = 8, max = 200) String newPassword
) {}
