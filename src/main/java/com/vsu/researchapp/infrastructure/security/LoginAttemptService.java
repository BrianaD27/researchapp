package com.vsu.researchapp.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private static final Logger logger =
        LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final int BLOCK_DURATION_MINUTES = 15;
    private static final String PREFIX = "login_attempts:";

    private final RedisTemplate<String, Object> redisTemplate;

    // Fallback in-memory if Redis is unavailable
    private final ConcurrentHashMap<String, LoginAttemptInfo> fallback =
        new ConcurrentHashMap<>();

    public LoginAttemptService(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void loginFailed(String username) {
        try {
            String key = PREFIX + username;
            Long attempts = redisTemplate.opsForValue().increment(key);
            if (attempts != null && attempts == 1) {
                redisTemplate.expire(key,
                    Duration.ofMinutes(BLOCK_DURATION_MINUTES));
            }
            logger.warn("[LOGIN] Failed attempt {} for user: {}",
                attempts, username);
        } catch (Exception e) {
            logger.error("[REDIS] Falling back to in-memory: {}",
                e.getMessage());
            fallback.merge(username,
                new LoginAttemptInfo(1, LocalDateTime.now()),
                (existing, newVal) -> {
                    existing.count++;
                    existing.lastAttempt = LocalDateTime.now();
                    return existing;
                });
        }
    }

    public void loginSucceeded(String username) {
        try {
            redisTemplate.delete(PREFIX + username);
        } catch (Exception e) {
            fallback.remove(username);
        }
    }

    public boolean isBlocked(String username) {
        try {
            String key = PREFIX + username;
            Object val = redisTemplate.opsForValue().get(key);
            if (val == null) return false;
            int attempts = Integer.parseInt(val.toString());
            return attempts >= MAX_ATTEMPTS;
        } catch (Exception e) {
            logger.error("[REDIS] Falling back to in-memory: {}",
                e.getMessage());
            LoginAttemptInfo info = fallback.get(username);
            if (info == null) return false;
            if (info.lastAttempt.isBefore(LocalDateTime.now()
                    .minusMinutes(BLOCK_DURATION_MINUTES))) {
                fallback.remove(username);
                return false;
            }
            return info.count >= MAX_ATTEMPTS;
        }
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
