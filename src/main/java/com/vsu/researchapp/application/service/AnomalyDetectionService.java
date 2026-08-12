package com.vsu.researchapp.application.service;

import com.vsu.researchapp.infrastructure.security.ThreatEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AnomalyDetectionService {

    private static final Logger logger =
        LoggerFactory.getLogger(AnomalyDetectionService.class);

    private static final String PREFIX = "anomaly:";
    private static final int REQUEST_THRESHOLD = 100;
    private static final int WINDOW_SECONDS = 60;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ThreatEventPublisher threatEventPublisher;

    public AnomalyDetectionService(
            RedisTemplate<String, Object> redisTemplate,
            ThreatEventPublisher threatEventPublisher) {
        this.redisTemplate = redisTemplate;
        this.threatEventPublisher = threatEventPublisher;
    }

    public void trackRequest(String username, String ip,
            String endpoint) {
        try {
            // Track requests per user per minute
            String userKey = PREFIX + "user:" + username;
            Long userCount = redisTemplate.opsForValue()
                .increment(userKey);
            if (userCount != null && userCount == 1) {
                redisTemplate.expire(userKey,
                    Duration.ofSeconds(WINDOW_SECONDS));
            }

            // Flag anomaly if over threshold
            if (userCount != null &&
                    userCount > REQUEST_THRESHOLD) {
                logger.warn("[ANOMALY] High request rate - " +
                    "User: {} IP: {} Count: {}",
                    username, ip, userCount);

                threatEventPublisher.publishThreatEvent(
                    "ANOMALY_DETECTED", username, ip,
                    "High request rate: " + userCount +
                    " requests/min on " + endpoint);
            }

            // Track failed logins per IP
            String ipKey = PREFIX + "ip:" + ip;
            Long ipCount = redisTemplate.opsForValue()
                .increment(ipKey);
            if (ipCount != null && ipCount == 1) {
                redisTemplate.expire(ipKey,
                    Duration.ofSeconds(WINDOW_SECONDS));
            }

        } catch (Exception e) {
            logger.error("[ANOMALY] Detection error: {}",
                e.getMessage());
        }
    }

    public void trackFailedLogin(String username, String ip) {
        try {
            String key = PREFIX + "failed:" + ip;
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key, Duration.ofMinutes(15));
            }

            if (count != null && count >= 10) {
                threatEventPublisher.publishThreatEvent(
                    "BRUTE_FORCE", username, ip,
                    "10+ failed logins from IP: " + ip);
            }
        } catch (Exception e) {
            logger.error("[ANOMALY] Failed login tracking: {}",
                e.getMessage());
        }
    }

    public void trackHoneypotHit(String ip, String path) {
        threatEventPublisher.publishThreatEvent(
            "HONEYPOT_TRIGGERED", null, ip,
            "Honeypot path accessed: " + path);
    }

    public void trackSqlInjection(String ip, String param) {
        threatEventPublisher.publishThreatEvent(
            "SQL_INJECTION", null, ip,
            "SQL injection attempt on param: " + param);
    }
}
