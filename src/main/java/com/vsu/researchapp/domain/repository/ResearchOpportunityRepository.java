package com.vsu.researchapp.domain.repository;
import com.vsu.researchapp.domain.model.ResearchOpportunity;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResearchOpportunityRepository extends JpaRepository<ResearchOpportunity, Long> {

        @Query("SELECT opp FROM ResearchOpportunity opp where opp.beginDate >= CURRENT_DATE ORDER BY opp.beginDate")
        List<ResearchOpportunity> getResearchOpportunitiesByUpcoming();

        @Query("SELECT opp FROM ResearchOpportunity opp WHERE opp.beginDate >= :earliestDate AND opp.endDate <= :latestDate")
        List<ResearchOpportunity> getResearchOpportunitiesByDateRange(@Param("earliestDate") LocalDate earliestDate, @Param("latestDate") LocalDate latestDate);

        @Query("SELECT opp FROM ResearchOpportunity opp WHERE opp.professor.name = :professorName")
        List<ResearchOpportunity> getResearchOpportunitiesByProfessorName(@Param("professorName") String professorName);
}
