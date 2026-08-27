package com.vsu.researchapp.presentation.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
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

    @PostMapping(value = "/api/students/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponseDto> uploadStudentProfilePicture(
            @PathVariable Long id, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadStudentProfilePicture(id, file));
    }

    @PostMapping(value = "/api/professors/{id}/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponseDto> uploadProfessorProfilePicture(
            @PathVariable Long id, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadProfessorProfilePicture(id, file));
    }

    @PostMapping(value = "/api/research-opportunities/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponseDto> uploadResearchMedia(
            @PathVariable Long id, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.uploadResearchMedia(id, file));
    }

    @GetMapping("/api/research-opportunities/{id}/media")
    public ResponseEntity<List<String>> listResearchMedia(@PathVariable Long id) {
        return ResponseEntity.ok(mediaUploadService.listResearchMedia(id));
    }

    @PutMapping(value = "/api/research-opportunities/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponseDto> replaceResearchMedia(
            @PathVariable Long id,
            @RequestParam("url") String oldMediaUrl,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mediaUploadService.replaceResearchMedia(id, oldMediaUrl, file));
    }

    @DeleteMapping("/api/research-opportunities/{id}/media")
    public ResponseEntity<Void> deleteResearchMedia(
            @PathVariable Long id, @RequestParam("url") String mediaUrl) {
        mediaUploadService.deleteResearchMedia(id, mediaUrl);
        return ResponseEntity.noContent().build();
    }
}
