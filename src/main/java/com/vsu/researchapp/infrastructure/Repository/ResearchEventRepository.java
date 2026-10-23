package com.vsu.researchapp.infrastructure.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsu.researchapp.domain.model.ResearchEvent;

public interface ResearchEventRepository extends JpaRepository<ResearchEvent, Long> {

    @Query("SELECT event FROM ResearchEvent event WHERE event.beginDate >= CURRENT_DATE ORDER BY event.beginDate")
    List<ResearchEvent> getResearchEventsByUpcoming();

    @Query("SELECT event FROM ResearchEvent event WHERE event.beginDate >= :earliestDate AND event.endDate <= :latestDate")
    List<ResearchEvent> getResearchEventsByDateRange(@Param("earliestDate") LocalDate earliestDate, @Param("latestDate") LocalDate latestDate);
}
