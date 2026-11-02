package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.AssociateProfile;

public interface AssociateProfileRepository extends JpaRepository<AssociateProfile, Long> {

    Optional<AssociateProfile> findByUserAccountId(Long userAccountId);
}
