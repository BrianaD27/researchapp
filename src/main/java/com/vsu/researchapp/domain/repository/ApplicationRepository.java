package com.vsu.researchapp.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}
