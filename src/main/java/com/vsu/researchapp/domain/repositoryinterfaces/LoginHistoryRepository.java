package com.vsu.researchapp.domain.repositoryinterfaces;
import com.vsu.researchapp.domain.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    List<LoginHistory> findByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findTop10ByUsernameOrderByLoginTimeDesc(String username);

    List<LoginHistory> findByUsernameAndStatusOrderByLoginTimeDesc(
        String username, String status);

    List<LoginHistory> findByStatusOrderByLoginTimeDesc(String status);

    List<LoginHistory> findTop50ByOrderByLoginTimeDesc();
}