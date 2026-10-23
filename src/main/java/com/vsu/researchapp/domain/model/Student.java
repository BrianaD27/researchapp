package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String major;
    private Integer graduationYear;
    private String classification;
    private String description;
    private String previousExperience;
    private Float gpa;
    private Integer availableHoursPerWeek;
    private String resumeUrl;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Links this profile back to the login account that owns it, so the backend
    // can tell "is the logged-in user allowed to edit THIS student record" instead
    // of trusting whatever id is in the URL. Null for records created before this
    // link existed (e.g. seed data) or created directly by an admin.
    private Long userAccountId;

    // Stores Skills as a separate table (Without creating a new entity table) with a foreign key to Student, allowing for multiple skills per student
    @ElementCollection
    @CollectionTable(name = "studentSkills", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "skill")
    private List<String> skills;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}