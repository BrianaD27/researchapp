package com.vsu.researchapp.presentation.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@RestController
@RequestMapping("/api/students/me/resume")
public class StudentResumeController {

    private final StudentRepositoryInterface students;
    private final CurrentUserService currentUser;
    private final Path directory;

    public StudentResumeController(
            StudentRepositoryInterface students,
            CurrentUserService currentUser,
            @Value("${resume.storage.path:./private-uploads/student-resumes}") String storagePath) {
        this.students = students;
        this.currentUser = currentUser;
        this.directory = Path.of(storagePath).toAbsolutePath().normalize();
    }

    private Student ownProfile() {
        Long accountId = currentUser.getCurrentUserAccount().getId();
        return students.getStudentByUserAccountId(accountId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Save your student profile first"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestPart("file") MultipartFile file) throws IOException {
        Student student = ownProfile();

        if (file.isEmpty() || file.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Choose a PDF up to 5 MB");
        }

        byte[] data = file.getBytes();
        if (!"application/pdf".equals(file.getContentType())
                || data.length < 5
                || data[0] != '%' || data[1] != 'P'
                || data[2] != 'D' || data[3] != 'F' || data[4] != '-') {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "The resume must be a PDF");
        }

        Files.createDirectories(directory);
        Path destination = directory.resolve(student.getId() + ".pdf");
        Path temporary = Files.createTempFile(directory, "resume-", ".tmp");
        try {
            Files.write(temporary, data);
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }

        String url = "/api/students/me/resume";
        student.setResumeUrl(url);
        students.updateStudent(student, student.getId());
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping
    public ResponseEntity<byte[]> download() throws IOException {
        Student student = ownProfile();
        Path file = directory.resolve(student.getId() + ".pdf");

        if (!Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No resume uploaded");
        }

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(Files.readAllBytes(file));
    }
}