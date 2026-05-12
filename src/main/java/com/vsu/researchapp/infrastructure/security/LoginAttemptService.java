package com.vsu.researchapp.infrastructure.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;

    private final ConcurrentHashMap<String, LoginAttemptInfo> attempts = 
        new ConcurrentHashMap<>();

    public void loginFailed(String username) {
        attempts.merge(username, new LoginAttemptInfo(1, LocalDateTime.now()),
            (existing, newVal) -> {
                existing.count++;
                existing.lastAttempt = LocalDateTime.now();
                return existing;
            });
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        LoginAttemptInfo info = attempts.get(username);
        if (info == null) return false;

        // Auto-unblock after 15 minutes
        if (info.lastAttempt.isBefore(LocalDateTime.now()
                .minusMinutes(BLOCK_DURATION_MINUTES))) {
            attempts.remove(username);
            return false;
        }

        return info.count >= MAX_ATTEMPTS;
    }

    private static class LoginAttemptInfo {
        int count;
        LocalDateTime lastAttempt;

        LoginAttemptInfo(int count, LocalDateTime lastAttempt) {
            this.count = count;
            this.lastAttempt = lastAttempt;
        }
    }
}
