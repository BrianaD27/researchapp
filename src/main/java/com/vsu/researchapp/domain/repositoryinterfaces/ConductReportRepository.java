package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.ConductReport;

public interface ConductReportRepository extends JpaRepository<ConductReport, Long> {

    List<ConductReport> findTop300ByOrderByCreatedAtDesc();

    long countByReporterAccountIdAndCreatedAtAfter(Long reporterAccountId, LocalDateTime after);

    boolean existsByReporterAccountIdAndMessageId(Long reporterAccountId, Long messageId);

    long countByStatusNot(String status);
}
