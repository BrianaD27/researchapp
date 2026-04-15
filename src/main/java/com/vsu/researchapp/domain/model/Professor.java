package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;

@Entity
@Table(name = "professors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String department;
    private String office_location;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profilePictureUrl;


    // Stores Research Media URLs as a separate table (Without creating a new entity table) with a foreign key to Professor, allowing for multiple media URLs per professor
    @ElementCollection
    @CollectionTable(name = "professor_research_media_urls", joinColumns = @JoinColumn(name = "professor_id"))
    @Column(name = "media_url")
    private List<String> researchMediaUrls;

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
