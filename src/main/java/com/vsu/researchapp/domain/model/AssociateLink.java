package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A Research Associate working for one professor, with what they may do there.
// An associate can have links to several professors (up to a limit) and switches
// between them in the app with "Working for".
@Entity
@Table(name = "associate_links",
    uniqueConstraints = @UniqueConstraint(columnNames = {"professorId", "associateUserAccountId"}))
@Getter
@Setter
@NoArgsConstructor
public class AssociateLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private Long associateUserAccountId;

    // Post and edit the professor's research opportunities and their media.
    @Column(nullable = false)
    private boolean canPublish = true;

    // See applicants, accept or decline them, and write mentor notes.
    @Column(nullable = false)
    private boolean canReview = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
