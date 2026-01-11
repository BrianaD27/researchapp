package com.vsu.researchapp.application.dto;

public record UpdateStudentDto(
    String name,
    String major,
    String description,
    String skills
) {}