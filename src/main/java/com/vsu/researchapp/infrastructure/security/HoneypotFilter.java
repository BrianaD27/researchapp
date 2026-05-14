package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@Component
public class HoneypotFilter extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(HoneypotFilter.class);

    private static final Set<String> HONEYPOT_PATHS = Set.of(
        "/admin",
        "/wp-admin",
        "/wp-login.php",
        "/phpmyadmin",
        "/manager/html",
        "/.env",
        "/config",
        "/backup",
        "/api/v1/internal",
        "/actuator/shutdown",
        "/console"
    );

    private final RedisTemplate<String, Object> redisTemplate;

    public HoneypotFilter(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = getClientIp(request);

        if (HONEYPOT_PATHS.stream().anyMatch(path::startsWith)) {
            logger.warn("[HONEYPOT] 🍯 Triggered by IP: {} Path: {}",
                ip, path);

            // Flag this IP in Redis for 24 hours
            try {
                redisTemplate.opsForValue().set(
                    "honeypot:" + ip,
                    path,
                    Duration.ofHours(24)
                );
            } catch (Exception e) {
                logger.error("[REDIS] Honeypot flag failed: {}",
                    e.getMessage());
            }

            response.setStatus(404);
            response.setHeader("Content-Type", "application/json");
            response.getWriter().write("{\"error\": \"Not found\"}");
            return;
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
}

