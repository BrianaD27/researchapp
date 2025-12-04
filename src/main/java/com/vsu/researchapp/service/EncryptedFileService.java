package com.vsu.researchapp.service;

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

@Service
public class EncryptedFileService {

    private final EncryptedFileRepository encryptedFileRepository;
    private final EncryptedFileShareRepository encryptedFileShareRepository;
    private final EncryptionService encryptionService;

    public EncryptedFileService(EncryptedFileRepository encryptedFileRepository,
                                EncryptedFileShareRepository encryptedFileShareRepository,
                                EncryptionService encryptionService) {
        this.encryptedFileRepository = encryptedFileRepository;
        this.encryptedFileShareRepository = encryptedFileShareRepository;
        this.encryptionService = encryptionService;
    }

    /**
     * Upload a file and store it encrypted.
     */
    public EncryptedFile uploadAndEncrypt(UserAccount owner,
                                          ResearchOpportunity opportunity,
                                          MultipartFile file,
                                          String passphrase) throws Exception {

        byte[] salt = encryptionService.generateRandomSalt();
        byte[] iv = encryptionService.generateRandomIv();
        byte[] encrypted = encryptionService.encrypt(file.getBytes(), passphrase, salt, iv);

        EncryptedFile entity = new EncryptedFile();
        entity.setOwner(owner);
        entity.setResearchOpportunity(opportunity);
        entity.setOriginalFilename(file.getOriginalFilename());
        entity.setContentType(file.getContentType());
        entity.setSize(file.getSize());
        entity.setSalt(salt);
        entity.setIv(iv);
        entity.setEncryptedData(encrypted);
        entity.setCreatedAt(Instant.now());

        return encryptedFileRepository.save(entity);
    }

    /**
     * Decrypt the stored file data using the passphrase.
     */
    public byte[] decryptFileData(Long fileId, String passphrase) throws Exception {
        EncryptedFile encryptedFile = encryptedFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Encrypted file not found: " + fileId));

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

    public EncryptedFileShare shareWithUser(EncryptedFile file, UserAccount targetUser) {
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
