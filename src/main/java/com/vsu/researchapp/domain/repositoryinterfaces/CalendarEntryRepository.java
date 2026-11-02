package com.vsu.researchapp.domain.repositoryinterfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.CalendarEntry;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {

    List<CalendarEntry> findByOwnerAccountIdOrderByStartDateAsc(Long ownerAccountId);

    Optional<CalendarEntry> findByIdAndOwnerAccountId(Long id, Long ownerAccountId);

    long countByOwnerAccountId(Long ownerAccountId);

    long countByOwnerAccountIdAndNotifiedTrueAndCreatedAtAfter(Long ownerAccountId, LocalDateTime after);
}
