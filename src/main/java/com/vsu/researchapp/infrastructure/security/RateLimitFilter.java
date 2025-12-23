package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Simple per-IP bucket: allow N requests per windowMillis
    private static final int LIMIT = 30;
    private static final long WINDOW_MILLIS = 60_000; // 60 seconds

    private static class Bucket {
        int count;
        long windowStart;
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only rate-limit sensitive routes
        boolean sensitive =
                path.startsWith("/api/admin") ||
                path.startsWith("/api/auth") ||
                path.startsWith("/login");

        if (!sensitive) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        long now = Instant.now().toEpochMilli();

        Bucket bucket = buckets.computeIfAbsent(ip, k -> {
            Bucket b = new Bucket();
            b.count = 0;
            b.windowStart = now;
            return b;
        });

        synchronized (bucket) {
            if (now - bucket.windowStart > WINDOW_MILLIS) {
                bucket.windowStart = now;
                bucket.count = 0;
            }

            bucket.count++;

          if (bucket.count > LIMIT) {
    response.setStatus(429);
    response.setContentType("application/json");
    response.getWriter().write("{\"error\":\"Too many requests. Try again later.\"}");
    return;
}

        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
