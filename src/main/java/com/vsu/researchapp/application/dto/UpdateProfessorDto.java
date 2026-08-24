package com.vsu.researchapp.application.dto;

public record UpdateProfessorDto(
    String name,
    String department,
    String officeLocation,
    String description,
    String profilePictureUrl
) {}
