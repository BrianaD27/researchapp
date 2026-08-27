package com.vsu.researchapp.application.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vsu.researchapp.application.dto.MediaUploadResponseDto;
import com.vsu.researchapp.domain.exception.InvalidFileException;
import com.vsu.researchapp.domain.model.Professor;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.Student;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.FileStorageInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ProfessorRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.ResearchOpportunityRepositoryInterface;
import com.vsu.researchapp.domain.repositoryinterfaces.StudentRepositoryInterface;
import com.vsu.researchapp.infrastructure.security.CurrentUserService;

@Service
public class MediaUploadService {

    private static final Set<String> PROFILE_PICTURE_TYPES = Set.of(
        "image/png", "image/jpeg"
    );

    private static final long PROFILE_PICTURE_MAX_SIZE = 5 * 1024 * 1024;

   private static final Set<String> RESEARCH_MEDIA_TYPES = Set.of(
        "application/pdf",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "image/png",
        "image/jpeg"
    );

    private static final long RESEARCH_MEDIA_MAX_SIZE = 20 * 1024 * 1024;

    private final FileStorageInterface fileStorage;
    private final ProfessorRepositoryInterface professorRepository;
    private final StudentRepositoryInterface studentRepository;
    private final ResearchOpportunityRepositoryInterface researchOpportunityRepository;
    private final CurrentUserService currentUserService;

    public MediaUploadService(FileStorageInterface fileStorage, ProfessorRepositoryInterface professorRepository, StudentRepositoryInterface studentRepository, ResearchOpportunityRepositoryInterface researchOpportunityRepository, CurrentUserService currentUserService) {
        this.fileStorage = fileStorage;
        this.professorRepository = professorRepository;
        this.researchOpportunityRepository = researchOpportunityRepository;
        this.studentRepository = studentRepository;
        this.currentUserService = currentUserService;
    }

    public MediaUploadResponseDto uploadStudentProfilePicture(Long studentId, MultipartFile file) throws IOException {
        Student student = studentRepository.getStudentById(studentId);
        assertCanModifyStudent(student);

        // Validate File size
        validateFile(file, PROFILE_PICTURE_TYPES, PROFILE_PICTURE_MAX_SIZE);

        // Store the new picture first, so a failed upload never leaves the student
        // without any picture at all.
        String url = fileStorage.store(file, "profile-pictures");
        String oldUrl = student.getProfilePictureUrl();

        // set student profile picture
        student.setProfilePictureUrl(url);

        // update student
        studentRepository.updateStudent(student, studentId);

        // Only now remove the old file, once the new one is safely stored and saved.
        if (oldUrl != null) {
            fileStorage.delete(oldUrl);
        }

        // return the typed response instead of a bare URL string
        return new MediaUploadResponseDto(url, file.getOriginalFilename(), file.getContentType(), file.getSize());

    }

    public MediaUploadResponseDto uploadProfessorProfilePicture(Long professorId, MultipartFile file) throws IOException {
        Professor professor = professorRepository.getProfessorById(professorId);
        assertCanModifyProfessor(professor);

        validateFile(file, PROFILE_PICTURE_TYPES, PROFILE_PICTURE_MAX_SIZE);

        // Store the new picture first, so a failed upload never leaves the professor
        // without any picture at all.
        String url = fileStorage.store(file, "profile-pictures");
        String oldUrl = professor.getProfilePictureUrl();

        professor.setProfilePictureUrl(url);
        professorRepository.updateProfessor(professor, professorId);

        // Only now remove the old file, once the new one is safely stored and saved.
        if (oldUrl != null) {
            fileStorage.delete(oldUrl);
        }

        return new MediaUploadResponseDto(url, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    public MediaUploadResponseDto uploadResearchMedia(Long researchOpportunityId, MultipartFile file) throws IOException {
        ResearchOpportunity researchOpportunity = researchOpportunityRepository.getResearchOpportunity(researchOpportunityId);
        assertCanModifyResearchOpportunity(researchOpportunity);

        // Validate file type and size
        validateFile(file, RESEARCH_MEDIA_TYPES, RESEARCH_MEDIA_MAX_SIZE);

        // Store the file in the appropriate folder
        String url = fileStorage.store(file, "research-media");

        // Add the new media URL to the existing list, initializing it if this is the
        // first media file ever attached to this opportunity (the collection is null
        // until the first item is added).
        List<String> existingMediaUrls = researchOpportunity.getResearchMediaUrls();
        if (existingMediaUrls == null) {
            existingMediaUrls = new ArrayList<>();
        }
        existingMediaUrls.add(url);

        researchOpportunity.setResearchMediaUrls(existingMediaUrls);
        researchOpportunityRepository.updateResearchOpportunity(researchOpportunity, researchOpportunityId);

        return new MediaUploadResponseDto(url, file.getOriginalFilename(), file.getContentType(), file.getSize());
    }

    public void deleteResearchMedia(Long researchOpportunityId, String mediaUrl) {
        ResearchOpportunity researchOpportunity = researchOpportunityRepository.getResearchOpportunity(researchOpportunityId);
        assertCanModifyResearchOpportunity(researchOpportunity);

        List<String> existingMediaUrls = researchOpportunity.getResearchMediaUrls();
        if (existingMediaUrls == null || !existingMediaUrls.remove(mediaUrl)) {
            throw new InvalidFileException("No media matching that URL is attached to this research opportunity");
        }

        researchOpportunity.setResearchMediaUrls(existingMediaUrls);
        researchOpportunityRepository.updateResearchOpportunity(researchOpportunity, researchOpportunityId);

        // Only remove the stored file once the entity no longer references it.
        fileStorage.delete(mediaUrl);
    }

    private void assertCanModifyStudent(Student student) {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (student.getUserAccountId() == null
                || !student.getUserAccountId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only modify your own student profile");
        }
    }

    private void assertCanModifyResearchOpportunity(ResearchOpportunity researchOpportunity) {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        Professor owningProfessor = researchOpportunity.getProfessor();
        if (owningProfessor == null || owningProfessor.getUserAccountId() == null
                || !owningProfessor.getUserAccountId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only modify media on your own research opportunity");
        }
    }

    private void assertCanModifyProfessor(Professor professor) {
        UserAccount currentUser = currentUserService.getCurrentUserAccount();
        if (currentUserService.isAdmin(currentUser)) {
            return;
        }
        if (professor.getUserAccountId() == null
                || !professor.getUserAccountId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only modify your own professor profile");
        }
    }

    private void validateFile(MultipartFile file, Set<String> allowedTypes, long maxSize) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("No file was uploaded");
        }

        if (file.getSize() > maxSize) {
            throw new InvalidFileException(
                "File size exceeds maximum allowed size of " + (maxSize / (1024 * 1024)) + "MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new InvalidFileException("File type not allowed: " + contentType);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..")
                || filename.contains("/") || filename.contains("\\")) {
            throw new InvalidFileException("Invalid filename");
        }
    }
}

