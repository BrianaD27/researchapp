package com.vsu.researchapp.presentation.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vsu.researchapp.application.dto.MediaUploadResponseDto;
import com.vsu.researchapp.application.service.MediaUploadService;

@RestController
public class MediaController {

    private final MediaUploadService mediaUploadService;

    public MediaController(MediaUploadService mediaUploadService) {
        this.mediaUploadService = mediaUploadService;
    }

    @PostMapping("/api/students/{id}/profile-picture")
    public ResponseEntity<MediaUploadResponseDto> uploadStudentProfilePicture(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadStudentProfilePicture(id, file));
    }

    @PostMapping("/api/professors/{id}/profile-picture")
    public ResponseEntity<MediaUploadResponseDto> uploadProfessorProfilePicture(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadProfessorProfilePicture(id, file));
    }

    @PostMapping("/api/research-opportunities/{id}/media")
    public ResponseEntity<MediaUploadResponseDto> uploadResearchMedia(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadResearchMedia(id, file));
    }

    @DeleteMapping("/api/research-opportunities/{id}/media")
    public ResponseEntity<Void> deleteResearchMedia(
            @PathVariable Long id, @RequestParam("url") String mediaUrl) {
        mediaUploadService.deleteResearchMedia(id, mediaUrl);
        return ResponseEntity.noContent().build();
    }
}
