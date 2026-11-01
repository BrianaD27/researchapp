package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A partner school whose people can sign up with their own .edu email and view
// VSU research (Level 1: view only). Admins add and switch schools on or off.
@Entity
@Table(name = "partner_schools")
@Getter
@Setter
@NoArgsConstructor
public class PartnerSchool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    // e.g. "vt.edu". Also covers subdomains such as "students.vt.edu".
    @Column(nullable = false, length = 100, unique = true)
    private String emailDomain;

    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
