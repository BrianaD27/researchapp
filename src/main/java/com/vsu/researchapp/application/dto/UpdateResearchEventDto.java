package com.vsu.researchapp.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class UpdateResearchEventDto {
    private String title;
    private String description;
    private String address;
    private String registrationLink;
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    public UpdateResearchEventDto() {}

     // Getters and Setters
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegistrationLink() {
        return registrationLink;
    }
    public void setRegistrationLink(String registrationLink) {
        this.registrationLink = registrationLink;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }
    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

}