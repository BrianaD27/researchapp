package com.vsu.researchapp.domain.model;

import jakarta.persistence.*;

@Entity
public class StudentApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the research opportunity
    @ManyToOne(optional = false)
    private ResearchOpportunity opportunity;

    // For now, keep student info simple (no complex user linking yet)
    private String studentName;
    private String studentEmail;

    // PENDING / ACCEPTED / REJECTED
    private String status;

    // Optional message from the student
    @Column(length = 2000)
    private String message;

    // ===== Getters and Setters =====

    public Long getId() {
        return id;
    }

    public ResearchOpportunity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(ResearchOpportunity opportunity) {
        this.opportunity = opportunity;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}