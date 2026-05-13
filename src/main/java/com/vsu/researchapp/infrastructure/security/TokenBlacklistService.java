package com.vsu.researchapp.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private static final Logger logger =
        LoggerFactory.getLogger(TokenBlacklistService.class);

    private static final String PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    // Fallback in-memory if Redis unavailable
    private final ConcurrentHashMap<String, Boolean> fallback =
        new ConcurrentHashMap<>();

    public TokenBlacklistService(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Blacklist a token for the remaining duration
    public void blacklist(String token, long expirationMs) {
        try {
            redisTemplate.opsForValue().set(
                PREFIX + token,
                "blacklisted",
                Duration.ofMillis(expirationMs)
            );
            logger.info("[TOKEN] Token blacklisted");
        } catch (Exception e) {
            logger.error("[REDIS] Blacklist falling back to memory: {}",
                e.getMessage());
            fallback.put(token, true);
        }
    }

    // Check if token is blacklisted
    public boolean isBlacklisted(String token) {
        try {
            return redisTemplate.hasKey(PREFIX + token);
        } catch (Exception e) {
            logger.error("[REDIS] Blacklist check falling back to memory: {}",
                e.getMessage());
            return fallback.containsKey(token);
        }
    }
}
