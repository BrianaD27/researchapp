package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// An associate's request to take down one of the professor's opportunities.
// Nothing is removed until the professor (or an admin) approves it.
@Entity
@Table(name = "removal_requests")
@Getter
@Setter
@NoArgsConstructor
public class RemovalRequest {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String DENIED = "DENIED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long opportunityId;

    @Column(length = 255)
    private String opportunityTitle;

    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private Long requestedByUserAccountId;

    @Column(length = 100)
    private String requestedByUsername;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 20)
    private String status = PENDING;

    @Column(length = 300)
    private String outcome;

    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
