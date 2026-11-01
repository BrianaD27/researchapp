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

// One 1:1 thread between a student and a professor. The professor's Research
// Associates (with review permission) take part on the professor's side.
@Entity
@Table(name = "conversations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"studentUserAccountId", "professorId"}))
@Getter
@Setter
@NoArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentUserAccountId;

    @Column(nullable = false)
    private Long professorId;

    @Column(nullable = false)
    private Long professorUserAccountId;

    private LocalDateTime createdAt;
    private LocalDateTime lastMessageAt;

    @Column(length = 200)
    private String lastMessagePreview;

    // When each side last opened the thread; newer messages from the other side are unread.
    private LocalDateTime studentLastReadAt;
    private LocalDateTime facultyLastReadAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.lastMessageAt == null) this.lastMessageAt = this.createdAt;
    }
}
