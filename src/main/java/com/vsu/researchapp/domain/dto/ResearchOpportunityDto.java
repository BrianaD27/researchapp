package com.vsu.researchapp.domain.dto;

public class ResearchOpportunityDto {
    private Long id;
    private String title;
    private String description;
    private String field;

    public ResearchOpportunityDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
}
