package com.vsu.researchapp.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import jakarta.persistence.Column;

@Entity
@Table(name = "ResearchOpportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResearchOpportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String department;

    @ElementCollection
    @CollectionTable(name = "research_opportunity_required_classifications", joinColumns =  @JoinColumn(name = "research_opportunity_id")) 
    @Column(name = "classification")
    private List<String> requiredClassifications;

    private String availability;
    private Float minimumGpa;
    private LocalDate applicationDeadline;
    private LocalDate startDate;
    private LocalDate endDate;

    @ElementCollection
    @CollectionTable(name = "research_opportunity_required_majors", joinColumns = @JoinColumn(name = "research_opportunity_id"))
    @Column(name = "major")
    private List<String> requiredMajors;

    @ElementCollection
    @CollectionTable(name = "research_opportunity_required_skills", joinColumns = @JoinColumn(name = "research_opportunity_id"))
    @Column(name = "skill")
    private List<String> requiredSkills;


    // Stores Research Media URLs as a separate table (Without creating a new entity table) with a foreign key to Research Opportunity, allowing for multiple media URLs per opportunity
    @ElementCollection
    @CollectionTable(name = "research_opportunity_media_urls", joinColumns = @JoinColumn(name = "opportunity_id"))
    @Column(name = "media_url")
    private List<String> researchMediaUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


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