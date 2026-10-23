package com.vsu.researchapp.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


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
    private String officeLocation;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String profilePictureUrl;

    // Links this profile back to the login account that owns it - same reasoning
    // as Student.userAccountId. Lets the backend verify a PROFESSOR-role user is
    // only editing their own profile, not someone else's by guessing an id.
    private Long userAccountId;

    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL)
    private List<ResearchOpportunity> researchOpportunities;

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
