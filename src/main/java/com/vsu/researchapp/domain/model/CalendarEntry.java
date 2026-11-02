package com.vsu.researchapp.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One entry on a faculty member's or Research Associate's own calendar:
// office hours, an announcement, time away, anything they want to post.
@Entity
@Table(name = "calendar_entries", indexes = @Index(columnList = "ownerAccountId, startDate"))
@Getter
@Setter
@NoArgsConstructor
public class CalendarEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerAccountId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    // Last day for entries that span several days (null = one day).
    private LocalDate endDate;

    // Both null = all day.
    private LocalTime startTime;
    private LocalTime endTime;

    private boolean repeatWeekly;

    // Last day a weekly entry repeats (null = keeps repeating).
    private LocalDate repeatUntil;

    @Column(length = 300)
    private String location;

    @Column(length = 2000)
    private String notes;

    @Column(length = 20)
    private String color;

    // PUBLIC (shown on profiles) or PRIVATE (only the owner sees it).
    @Column(nullable = false, length = 10)
    private String visibility;

    // True for "Away" blocks: whole days marked unavailable (a research trip,
    // a conference). Boolean (not boolean) so the column can be added to an
    // existing table.
    private Boolean away;

    // True when students were sent a bell alert about this entry.
    private boolean notified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
