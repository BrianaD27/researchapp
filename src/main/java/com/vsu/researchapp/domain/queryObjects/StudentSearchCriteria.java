package com.vsu.researchapp.domain.queryObjects;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentSearchCriteria {
    private String input;
    private String major;
    private String classification;
    private Float gpa;
    private Integer availability;
    private List<String> skills;
}
