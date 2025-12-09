package com.vsu.researchapp.domain.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vsu.researchapp.domain.model.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);
}
