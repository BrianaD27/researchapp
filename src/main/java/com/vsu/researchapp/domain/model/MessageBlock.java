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

// blocker no longer gets messages from blocked, and blocked can't find blocker in search.
@Entity
@Table(name = "message_blocks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"blockerAccountId", "blockedAccountId"}))
@Getter
@Setter
@NoArgsConstructor
public class MessageBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blockerAccountId;

    @Column(nullable = false)
    private Long blockedAccountId;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
