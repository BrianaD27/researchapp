package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A "Report a Problem" submission. Status: NEW, IN_PROGRESS, FIXED.
@Entity
@Table(name = "problem_reports", indexes = @Index(columnList = "reporterUserAccountId, createdAt"))
@Getter
@Setter
@NoArgsConstructor
public class ProblemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterUserAccountId;

    @Column(nullable = false, length = 100)
    private String reporterUsername;

    @Column(nullable = false, length = 40)
    private String category;

    @Column(nullable = false, length = 2000)
    private String description;

    // App page the person was on, e.g. "/discover-opportunities".
    @Column(length = 255)
    private String page;

    @Column(length = 255)
    private String browser;

    @Column(nullable = false, length = 20)
    private String status = "NEW";

    @Column(length = 2000)
    private String adminReply;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
