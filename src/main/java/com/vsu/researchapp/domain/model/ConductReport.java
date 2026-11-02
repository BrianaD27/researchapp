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

// A confidential report about a message someone received. Only admins can see
// these. The reported person is never told who reported them.
@Entity
@Table(name = "conduct_reports", indexes = {
    @Index(columnList = "reporterAccountId, createdAt"),
    @Index(columnList = "reportedAccountId")})
@Getter
@Setter
@NoArgsConstructor
public class ConductReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long reporterAccountId;

    @Column(nullable = false)
    private Long reportedAccountId;

    private Long conversationId;
    private Long messageId;

    // Copies taken when the report was made, so editing or deleting later
    // can't change what the admins review.
    @Column(length = 2000)
    private String messageBody;

    @Column(length = 2000)
    private String context;

    @Column(length = 400)
    private String reason;

    // NEW, UNDER_REVIEW, or CLOSED
    @Column(nullable = false, length = 20)
    private String status = "NEW";

    // Set when closed: NO_ACTION, WARNING_SENT, or SUSPENDED
    @Column(length = 20)
    private String outcome;

    // Private to admins. Never sent to anyone.
    @Column(length = 2000)
    private String adminNotes;

    private LocalDateTime warningSentAt;
    private LocalDateTime closedAt;

    @Column(length = 100)
    private String closedBy;

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
