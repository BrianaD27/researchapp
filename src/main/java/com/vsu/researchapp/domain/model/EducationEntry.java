package com.vsu.researchapp.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One degree on a faculty profile, e.g. "Ph.D., Computer Engineering",
// "Virginia State University", "2012".
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {
    @Column(length = 150)
    private String degree;

    @Column(length = 150)
    private String school;

    @Column(length = 20)
    private String graduationYear;
}
