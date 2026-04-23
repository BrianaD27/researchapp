package com.vsu.researchapp.infrastructure.security;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginAttemptService {

    private final Map<String, Integer> attempts = new HashMap<>();
    private final int MAX_ATTEMPTS = 5;

    public void loginFailed(String username) {
        attempts.put(username, attempts.getOrDefault(username, 0) + 1);
    }

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public boolean isBlocked(String username) {
        return attempts.getOrDefault(username, 0) >= MAX_ATTEMPTS;
    }
}
