package com.vsu.researchapp.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "encrypted_files")
public class EncryptedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Original file name (what the user uploaded)
    private String originalFilename;

    // MIME type (pdf, docx, etc.)
    private String contentType;

    // Size in bytes
    private long size;

    // Who owns this file
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserAccount owner;

    // Optional: link to a research project if attached to one
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "research_opportunity_id")
    private ResearchOpportunity researchOpportunity;

    // When this encrypted file was created
    private Instant createdAt;

    // Salt used to derive the key from the passphrase
    @Lob
    private byte[] salt;

    // IV / nonce used for AES
    @Lob
    private byte[] iv;

    // The actual encrypted data
    @Lob
    private byte[] encryptedData;

    // === GETTERS & SETTERS ===

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public UserAccount getOwner() {
        return owner;
    }

    public void setOwner(UserAccount owner) {
        this.owner = owner;
    }

    public ResearchOpportunity getResearchOpportunity() {
        return researchOpportunity;
    }

    public void setResearchOpportunity(ResearchOpportunity researchOpportunity) {
        this.researchOpportunity = researchOpportunity;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }
}
