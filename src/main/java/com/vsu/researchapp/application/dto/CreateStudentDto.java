package com.vsu.researchapp.application.dto;

public record CreateStudentDto(
    String name,
    String email,
    Integer graduateYear,
    String major,
    String description,
    String skills
) {}