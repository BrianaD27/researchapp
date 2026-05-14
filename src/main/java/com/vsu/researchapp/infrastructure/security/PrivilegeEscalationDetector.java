package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
public class PrivilegeEscalationDetector extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(PrivilegeEscalationDetector.class);

    private static final String PREFIX = "priv_escalation:";
    private static final int MAX_ATTEMPTS = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public PrivilegeEscalationDetector(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        chain.doFilter(request, response);

        // Check after response — if 403 flag it
        if (response.getStatus() == 403) {
            Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                String username = auth.getName();
                String ip = request.getRemoteAddr();

                try {
                    String key = PREFIX + username;
                    Long count = redisTemplate.opsForValue()
                        .increment(key);
                    redisTemplate.expire(key, Duration.ofHours(1));

                    if (count != null && count >= MAX_ATTEMPTS) {
                        logger.warn("[PRIVILEGE ESCALATION] " +
                            "User: {} IP: {} attempts: {}",
                            username, ip, count);
                    }
                } catch (Exception e) {
                    logger.warn("[REDIS] Escalation detector: {}",
                        e.getMessage());
                }
            }
        }
    }
}
