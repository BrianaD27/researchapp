package com.vsu.researchapp.domain.repository;

import com.vsu.researchapp.domain.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Keep or adjust these based on what you need.
    // If you don't need them yet, you can even leave the interface empty
    // and just rely on the inherited JpaRepository methods.

    Optional<UserAccount> findByEmail(String email);

    Optional<UserAccount> findByUsername(String username);
}

