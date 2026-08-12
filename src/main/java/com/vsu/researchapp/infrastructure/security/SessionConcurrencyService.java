package com.vsu.researchapp.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SessionConcurrencyService {

    private static final Logger logger =
        LoggerFactory.getLogger(SessionConcurrencyService.class);

    private static final String PREFIX = "session:";
    private static final int MAX_SESSIONS = 3;

    private final RedisTemplate<String, Object> redisTemplate;

    public SessionConcurrencyService(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void registerSession(String username, String sessionId) {
        try {
            String key = PREFIX + username;
            redisTemplate.opsForList().rightPush(key, sessionId);
            redisTemplate.expire(key, Duration.ofDays(7));

            // Remove oldest session if over limit
            Long size = redisTemplate.opsForList().size(key);
            if (size != null && size > MAX_SESSIONS) {
                String removed = (String) redisTemplate
                    .opsForList().leftPop(key);
                logger.info("[SESSION] Removed oldest session for " +
                    "user: {} session: {}", username, removed);
            }
        } catch (Exception e) {
            logger.warn("[SESSION] Redis unavailable: {}",
                e.getMessage());
        }
    }

    public void removeSession(String username, String sessionId) {
        try {
            redisTemplate.opsForList().remove(
                PREFIX + username, 1, sessionId);
        } catch (Exception e) {
            logger.warn("[SESSION] Could not remove session: {}",
                e.getMessage());
        }
    }

    public long getActiveSessionCount(String username) {
        try {
            Long size = redisTemplate.opsForList()
                .size(PREFIX + username);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
