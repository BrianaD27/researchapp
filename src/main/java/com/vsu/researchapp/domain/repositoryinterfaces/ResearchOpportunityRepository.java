package com.vsu.researchapp.domain.repositoryinterfaces;
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

        //Search
        @Query("SELECT opp FROM ResearchOpportunity opp WHERE " + "LOWER(opp.title) LIKE LOWER(CONCAT('%', :term, '%')) OR " + "LOWER(opp.createdBy.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " + "LOWER(opp.createdBy.department) LIKE LOWER(CONCAT('%', :term, '%'))")
        List<ResearchOpportunity> searchOpportunities(@Param("term") String term);
}
