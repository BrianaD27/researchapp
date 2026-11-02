package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A Research Associate's own profile (doctoral candidates, research staff).
// Which professors they work for, and what they may do, lives in AssociateLink.
@Entity
@Table(name = "associate_profiles")
@Getter
@Setter
@NoArgsConstructor
public class AssociateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userAccountId;

    @Column(length = 150)
    private String name;

    // e.g. "Doctoral Candidate", "Research Coordinator"
    @Column(length = 150)
    private String title;

    @Column(length = 150)
    private String department;

    @Column(length = 2000)
    private String about;

    @Column(length = 500)
    private String profilePictureUrl;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
