package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.FacultyRequest;

public interface FacultyRequestRepository extends JpaRepository<FacultyRequest, Long> {

    List<FacultyRequest> findByStatusOrderByCreatedAtAsc(String status);

    List<FacultyRequest> findByStatusAndAdminsAlertedFalse(String status);

    Optional<FacultyRequest> findByUserAccountId(Long userAccountId);
}
