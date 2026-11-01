package com.vsu.researchapp.application.dto;

import java.time.LocalDateTime;

public record NotificationDto(
    Long id,
    String type,
    String message,
    String link,
    boolean read,
    LocalDateTime createdAt
) {}
