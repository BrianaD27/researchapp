package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateResearchEventDto(
    String title,
    String description,
    String address,
    String registrationLink,
    LocalDate beginDate,
    LocalDate endDate,
    LocalTime startTime,
    LocalTime endTime
) {}
