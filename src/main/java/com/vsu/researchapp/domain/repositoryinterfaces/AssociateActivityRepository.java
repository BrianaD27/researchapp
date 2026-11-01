package com.vsu.researchapp.domain.repositoryinterfaces;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vsu.researchapp.domain.model.AssociateActivity;

public interface AssociateActivityRepository extends JpaRepository<AssociateActivity, Long> {

    List<AssociateActivity> findTop50ByProfessorIdOrderByCreatedAtDesc(Long professorId);
}
