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

// A faculty sign-up waiting for an admin. The account has the PENDING_FACULTY
// role (no access to anything) until an admin approves it.
@Entity
@Table(name = "faculty_requests")
@Getter
@Setter
@NoArgsConstructor
public class FacultyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userAccountId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 150)
    private String department;

    // PENDING, APPROVED, or DECLINED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // Admins are alerted once, after the person verifies their VSU email.
    private boolean adminsAlerted = false;

    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    @Column(length = 100)
    private String decidedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
