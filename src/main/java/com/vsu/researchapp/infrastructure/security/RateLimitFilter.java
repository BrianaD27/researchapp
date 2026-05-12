package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMIT = 50;
    private static final int WINDOW_MINUTES = 1;

    private final ConcurrentHashMap<String, RequestInfo> requests = 
        new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, 
            HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String ip = getClientIp(req);
        LocalDateTime now = LocalDateTime.now();

        requests.merge(ip, new RequestInfo(1, now), (existing, newVal) -> {
            // Reset window if it's been more than 1 minute
            if (existing.windowStart.isBefore(
                    now.minusMinutes(WINDOW_MINUTES))) {
                existing.count = 1;
                existing.windowStart = now;
            } else {
                existing.count++;
            }
            return existing;
        });

        RequestInfo info = requests.get(ip);

        if (info.count > LIMIT) {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.setHeader("Content-Type", "application/json");
            res.getWriter().write(
                "{\"error\": \"Too many requests. Try again in 60 seconds.\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private static class RequestInfo {
        int count;
        LocalDateTime windowStart;

        RequestInfo(int count, LocalDateTime windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}