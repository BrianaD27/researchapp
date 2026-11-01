package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Audit trail: what an associate did on a professor's behalf.
@Entity
@Table(name = "associate_activity", indexes = @Index(columnList = "professorId, createdAt"))
@Getter
@Setter
@NoArgsConstructor
public class AssociateActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false, length = 100)
    private String actorUsername;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(length = 500)
    private String detail;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
