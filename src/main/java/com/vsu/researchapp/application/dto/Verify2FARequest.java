package com.vsu.researchapp.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record Verify2FARequest(
    @NotBlank @Size(max = 100) String username,
    @NotBlank @Pattern(regexp = "\\d{6}") String code
) {}
