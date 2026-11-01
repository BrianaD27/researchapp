package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.PartnerSchool;

public interface PartnerSchoolRepository extends JpaRepository<PartnerSchool, Long> {

    List<PartnerSchool> findAllByOrderByNameAsc();

    List<PartnerSchool> findByActiveTrueOrderByNameAsc();

    Optional<PartnerSchool> findByEmailDomainIgnoreCase(String emailDomain);
}
