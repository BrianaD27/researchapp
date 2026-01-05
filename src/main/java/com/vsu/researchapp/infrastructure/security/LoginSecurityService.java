package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginSecurityService {

    private final UserAccountRepository repo;

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    public LoginSecurityService(UserAccountRepository repo) {
        this.repo = repo;
    }

    public void onLoginSuccess(String username) {
        repo.findByUsername(username).ifPresent(acc -> {
            acc.resetLock();
            repo.save(acc);
        });
    }

    public void onLoginFailure(String username) {
        repo.findByUsername(username).ifPresent(acc -> {
            acc.setFailedAttempts(acc.getFailedAttempts() + 1);
            acc.setLastFailedLogin(LocalDateTime.now());

            if (acc.getFailedAttempts() >= MAX_ATTEMPTS) {
                acc.setLockUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
            }

            repo.save(acc);
        });
    }
}

