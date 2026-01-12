package com.vsu.researchapp.application.dto;

public record UpdateStudentDto(
    String name,
    String major,
    Integer graduateYear,
    String description,
    String skills
) {}