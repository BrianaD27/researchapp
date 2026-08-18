package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int LIMIT = 50;
    private static final int WINDOW_SECONDS = 60;
    private static final String PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    // Fallback in-memory if Redis unavailable
    private final ConcurrentHashMap<String, FallbackWindow> fallback =
        new ConcurrentHashMap<>();

    public RateLimitFilter(
            RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String key = PREFIX + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key,
                    Duration.ofSeconds(WINDOW_SECONDS));
            }

            if (count != null && count > LIMIT) {
                logger.warn("[RATE LIMIT] IP blocked: {}", ip);
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setHeader("Content-Type", "application/json");
                response.getWriter().write(
                    "{\"error\": \"Too many requests. Try again in 60 seconds.\"}");
                return;
            }
        } catch (Exception e) {
            logger.error("[REDIS] Rate limit falling back to memory: {}",
                e.getMessage());
            long now = System.currentTimeMillis();
            FallbackWindow window = fallback.compute(ip, (key2, existing) -> {
                if (existing == null
                        || now - existing.startedAt >= WINDOW_SECONDS * 1000L) {
                    return new FallbackWindow(now, 1);
                }
                existing.count++;
                return existing;
            });
            if (window.count > LIMIT) {
                response.setStatus(429);
                response.setHeader("Retry-After", "60");
                response.setHeader("Content-Type", "application/json");
                response.getWriter().write(
                    "{\"error\": \"Too many requests.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class FallbackWindow {
        private final long startedAt;
        private int count;

        private FallbackWindow(long startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
