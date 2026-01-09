package com.vsu.researchapp.domain.model;


import jakarta.persistence.*;
import com.vsu.researchapp.infrastructure.security.EncryptedStringConverter;

@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 200)
    private String title;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(columnDefinition = "TEXT")
    private String applicationNotes;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getApplicationNotes() { return applicationNotes; }
    public void setApplicationNotes(String applicationNotes) {
        this.applicationNotes = applicationNotes;
    }
}
