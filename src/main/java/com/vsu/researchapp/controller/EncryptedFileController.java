package com.vsu.researchapp.controller;

import com.vsu.researchapp.domain.model.EncryptedFile;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.service.EncryptedFileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;


// NOTE: later we will wire this to real logged-in user from Security
@RestController
@RequestMapping("/api/encrypted-files")
@CrossOrigin(origins = "*")
public class EncryptedFileController {

   private final EncryptedFileService encryptedFileService;

public EncryptedFileController(EncryptedFileService encryptedFileService) {
    this.encryptedFileService = encryptedFileService;
}

    // TEMP: dummy method to get a fake current user
    // Later this will come from Spring Security (JWT/session)
    private UserAccount getCurrentUserStub() {
        UserAccount user = new UserAccount();
        // user.setId(1L); placeholder, we'll replace later
        return user;
    }

    @PostMapping("/upload")
    public ResponseEntity<EncryptedFile> uploadEncryptedFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("passphrase") String passphrase) {

        try {
            UserAccount owner = getCurrentUserStub();

            EncryptedFile saved = encryptedFileService.uploadAndEncrypt(
                    owner,
                    null, // no research project yet in this simple version
                    file,
                    passphrase
            );

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDecryptedFile(
            @PathVariable Long id,
            @RequestParam("passphrase") String passphrase) {

        try {
            byte[] data = encryptedFileService.decryptFileData(id, passphrase);
            // For now we just send generic octet-stream; later we can detect real contentType
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"decrypted-file\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.status(403).build(); // wrong passphrase / other error
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<EncryptedFile>> listMyEncryptedFiles() {
        UserAccount owner = getCurrentUserStub();
        List<EncryptedFile> files = encryptedFileService.getFilesForOwner(owner);
        return ResponseEntity.ok(files);
    }
}
