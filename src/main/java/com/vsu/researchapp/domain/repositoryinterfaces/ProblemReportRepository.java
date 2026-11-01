package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.ProblemReport;

public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {

    List<ProblemReport> findTop200ByOrderByCreatedAtDesc();

    List<ProblemReport> findTop50ByReporterUserAccountIdOrderByCreatedAtDesc(Long reporterUserAccountId);

    long countByReporterUserAccountIdAndCreatedAtAfter(Long reporterUserAccountId, LocalDateTime after);
}
