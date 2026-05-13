package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findTop10ByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findByUsernameAndStatusOrderByLoginTimeDesc(
        String username, String status);
}