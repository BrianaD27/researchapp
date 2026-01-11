package com.vsu.researchapp.application.dto;

public record UpdateStudentDto(
    String name,
    String email,
    String major,
    String description,
    String skills
) {}