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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A 1:1 conversation between any two VSU accounts (students, faculty,
// Research Associates, admins). The pair is stored lowest account id first so
// there is only ever one conversation per pair.
@Entity
@Table(name = "direct_conversations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userLowId", "userHighId"}),
    indexes = {@Index(columnList = "userLowId"), @Index(columnList = "userHighId")})
@Getter
@Setter
@NoArgsConstructor
public class DirectConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userLowId;

    @Column(nullable = false)
    private Long userHighId;

    // Who started it (used to limit how many new conversations one person opens a day).
    private Long createdBy;

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    @Column(length = 200)
    private String lastMessagePreview;

    private LocalDateTime lowLastReadAt;
    private LocalDateTime highLastReadAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
