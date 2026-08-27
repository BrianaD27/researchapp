package com.vsu.researchapp.application.dto;

public record MediaUploadResponseDto(
    String url, 
    String originalFilename,
    String contentType,
    long size
) {}
