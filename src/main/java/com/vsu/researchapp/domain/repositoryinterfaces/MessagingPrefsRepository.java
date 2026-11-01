package com.vsu.researchapp.domain.repositoryinterfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.MessagingPrefs;

public interface MessagingPrefsRepository extends JpaRepository<MessagingPrefs, Long> {
}
