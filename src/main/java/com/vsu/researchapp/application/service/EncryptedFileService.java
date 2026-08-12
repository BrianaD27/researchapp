package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.EncryptedFile;
import com.vsu.researchapp.domain.model.EncryptedFileShare;
import com.vsu.researchapp.domain.model.ResearchOpportunity;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.EncryptedFileRepository;
import com.vsu.researchapp.domain.repository.EncryptedFileShareRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class EncryptedFileService {

    // Allowed file types
    private static final Set<String> ALLOWED_TYPES = Set.of(
        "application/pdf",
        "image/jpeg",
        "image/png",
        "image/gif",
        "text/plain",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    // Max file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final EncryptedFileRepository encryptedFileRepository;
    private final EncryptedFileShareRepository encryptedFileShareRepository;
    private final EncryptionService encryptionService;

    public EncryptedFileService(
            EncryptedFileRepository encryptedFileRepository,
            EncryptedFileShareRepository encryptedFileShareRepository,
            EncryptionService encryptionService) {
        this.encryptedFileRepository = encryptedFileRepository;
        this.encryptedFileShareRepository = encryptedFileShareRepository;
        this.encryptionService = encryptionService;
    }

    public EncryptedFile uploadAndEncrypt(UserAccount owner,
                                          ResearchOpportunity opportunity,
                                          MultipartFile file,
                                          String passphrase) throws Exception {
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                "File size exceeds maximum allowed size of 10MB");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException(
                "File type not allowed: " + contentType);
        }

        // Validate filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.contains("..") ||
                filename.contains("/") || filename.contains("\\")) {
            throw new RuntimeException("Invalid filename");
        }

        // Encrypt and store
        byte[] salt = encryptionService.generateRandomSalt();
        byte[] iv = encryptionService.generateRandomIv();
        byte[] encrypted = encryptionService.encrypt(
            file.getBytes(), passphrase, salt, iv);

        EncryptedFile entity = new EncryptedFile();
        entity.setOwner(owner);
        entity.setResearchOpportunity(opportunity);
        entity.setOriginalFilename(filename);
        entity.setContentType(contentType);
        entity.setSize(file.getSize());
        entity.setSalt(salt);
        entity.setIv(iv);
        entity.setEncryptedData(encrypted);
        entity.setCreatedAt(Instant.now());

        return encryptedFileRepository.save(entity);
    }

    public byte[] decryptFileData(Long fileId,
            String passphrase) throws Exception {
        EncryptedFile encryptedFile = encryptedFileRepository
            .findById(fileId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Encrypted file not found: " + fileId));

        return encryptionService.decrypt(
            encryptedFile.getEncryptedData(),
            passphrase,
            encryptedFile.getSalt(),
            encryptedFile.getIv()
        );
    }

    public List<EncryptedFile> getFilesForOwner(UserAccount owner) {
        return encryptedFileRepository.findByOwner(owner);
    }

    public Optional<EncryptedFile> getById(Long id) {
        return encryptedFileRepository.findById(id);
    }

    public EncryptedFileShare shareWithUser(EncryptedFile file,
            UserAccount targetUser) {
        EncryptedFileShare share = new EncryptedFileShare();
        share.setEncryptedFile(file);
        share.setSharedWith(targetUser);
        share.setCreatedAt(Instant.now());
        return encryptedFileShareRepository.save(share);
    }

    public List<EncryptedFileShare> getSharesForUser(UserAccount user) {
        return encryptedFileShareRepository.findBySharedWith(user);
    }
}
