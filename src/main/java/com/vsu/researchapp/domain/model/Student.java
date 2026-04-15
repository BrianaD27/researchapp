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
    private Integer graduation_year;
    private String classification;
    private String description;
    private String previous_experience;
    private Float gpa;
    private Integer available_hours_per_week;
    private String resumeUrl;
    private String profile_picture_url;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    // Stores Skills as a separate table (Without creating a new entity table) with a foreign key to Student, allowing for multiple skills per student
    @ElementCollection
    @CollectionTable(name = "studentSkills", joinColumns = @JoinColumn(name = "student_id"))
    private List<String> skills;

    // Automatically Sets CreatedAt and Updated At on Created
    @PrePersist
    protected void onCreate() {
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }

    // Automatically sets UpdatedAt only when Updated
    @PreUpdate
    protected void onUpdate() {
        this.updated_at = LocalDateTime.now();
    }
}