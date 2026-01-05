package com.vsu.researchapp.application.dto;

public record StudentDto(
    Long id,
    String name,
    String email,
    String major
) {}
