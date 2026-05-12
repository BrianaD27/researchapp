package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class IpAllowlistFilter extends OncePerRequestFilter {

    private static final Logger logger = 
        LoggerFactory.getLogger(IpAllowlistFilter.class);

    // Only restrict admin routes
    private static final Set<String> PROTECTED_PATHS = Set.of(
        "/api/admin",
        "/actuator"
    );

    // Add VSU admin IPs here when you know them
    private static final Set<String> ALLOWED_ADMIN_IPS = Set.of(
        "127.0.0.1",
        "0:0:0:0:0:0:0:1"
        // Add VSU admin IP here e.g. "168.134.x.x"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = getClientIp(request);

        boolean isProtectedPath = PROTECTED_PATHS.stream()
            .anyMatch(path::startsWith);

        if (isProtectedPath && !ALLOWED_ADMIN_IPS.contains(ip)) {
            logger.warn("[IP BLOCK] Unauthorized access attempt | IP: {} | Path: {}",
                ip, path);
            response.setStatus(403);
            response.setHeader("Content-Type", "application/json");
            response.getWriter().write(
                "{\"error\": \"Access denied\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}