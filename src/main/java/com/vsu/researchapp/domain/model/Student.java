package com.vsu.researchapp.domain.model;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.CollectionTable;
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
import jakarta.persistence.Column;

@Entity
@Table(name = "students")
@Getter // Lombok annotation to generate getters for all fields 
@Setter // Lombok annotation to generate setters for all fields
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor
@AllArgsConstructor // Lombok annotation to generate an all-arguments constructor
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

    // Stores Skills as a separate table (Without creating a new entity table) with a foreign key to Student, allowing for multiple skills per student
    @ElementCollection
    @CollectionTable(name = "studentSkills", joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "skill")
    private List<String> skills;

    // Automatically Sets CreatedAt and Updated At on Created
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Automatically sets UpdatedAt only when Updated
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}