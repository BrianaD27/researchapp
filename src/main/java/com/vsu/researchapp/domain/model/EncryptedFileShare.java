package com.vsu.researchapp.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "encrypted_file_shares")
public class EncryptedFileShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The encrypted file being shared
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encrypted_file_id")
    private EncryptedFile encryptedFile;

    // Who this file is shared with
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_with_id")
    private UserAccount sharedWith;

    // When the share was created
    private Instant createdAt;

    // === GETTERS & SETTERS ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EncryptedFile getEncryptedFile() {
        return encryptedFile;
    }

    public void setEncryptedFile(EncryptedFile encryptedFile) {
        this.encryptedFile = encryptedFile;
    }

    public UserAccount getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(UserAccount sharedWith) {
        this.sharedWith = sharedWith;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
