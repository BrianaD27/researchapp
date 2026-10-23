package com.vsu.researchapp.infrastructure.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vsu.researchapp.domain.model.ResearchOpportunity;

public interface ResearchOpportunityJpaRepository extends JpaRepository<ResearchOpportunity, Long>, JpaSpecificationExecutor<ResearchOpportunity> {

    List<ResearchOpportunity> findByProfessorId(Long professorId);

    List<ResearchOpportunity> findByDepartmentIgnoreCase(String department);

    @Query("SELECT o FROM ResearchOpportunity o WHERE o.startDate >= CURRENT_DATE ORDER BY o.startDate")
    List<ResearchOpportunity> findUpcoming();

    @Query("SELECT o FROM ResearchOpportunity o WHERE o.startDate >= :earliestDate AND o.endDate <= :latestDate")
    List<ResearchOpportunity> findByDateRange(@Param("earliestDate") LocalDate earliestDate, @Param("latestDate") LocalDate latestDate);

    @Query("SELECT o FROM ResearchOpportunity o WHERE " +
        "LOWER(o.title) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(o.department) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(o.professor.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
        "LOWER(o.professor.department) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<ResearchOpportunity> searchByTerm(@Param("term") String term);

    @Query("SELECT o FROM ResearchOpportunity o WHERE o.applicationDeadline >= CURRENT_DATE ORDER BY o.applicationDeadline")
    List<ResearchOpportunity> findOpenForApplications();
}
