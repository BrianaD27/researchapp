package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record UserAccountDto(
    Long id,
    String username,
    String email,
    String role,
    boolean active,
    boolean accountLocked,
    boolean twoFactorEnabled,
    LocalDateTime lastLoginAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
