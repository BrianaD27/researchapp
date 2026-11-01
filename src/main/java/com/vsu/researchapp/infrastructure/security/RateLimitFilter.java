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

// Two limits per IP address, per minute:
// - Sign-in style requests (login, sign-up, password reset, 2FA, email
//   verification) stay strict, so passwords and codes can't be guessed quickly.
// - Everything else is generous, so normal clicking around the app (several
//   requests per page, plus the bell, messages, and green-dot checks) never trips
//   it, even with several people using the same computer during a demo.
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(RateLimitFilter.class);

    private static final int AUTH_LIMIT = 40;
    private static final int GENERAL_LIMIT = 600;
    private static final int WINDOW_SECONDS = 60;
    private static final String PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    // Fallback in memory if Redis is unavailable. Each entry remembers which
    // minute it belongs to, so the count resets every minute like Redis does.
    private final ConcurrentHashMap<String, long[]> fallback =
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
        boolean auth = isAuthRequest(request.getRequestURI());
        int limit = auth ? AUTH_LIMIT : GENERAL_LIMIT;
        String key = PREFIX + (auth ? "auth:" : "general:") + ip;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                redisTemplate.expire(key,
                    Duration.ofSeconds(WINDOW_SECONDS));
            }

            if (count != null && count > limit) {
                block(response, ip, auth);
                return;
            }
        } catch (Exception e) {
            logger.error("[REDIS] Rate limit falling back to memory: {}",
                e.getMessage());
            long minute = System.currentTimeMillis() / (WINDOW_SECONDS * 1000L);
            long[] slot = fallback.compute(key, (k, current) ->
                current == null || current[0] != minute ? new long[] {minute, 0} : current);
            long count;
            synchronized (slot) {
                count = ++slot[1];
            }
            if (count > limit) {
                block(response, ip, auth);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static boolean isAuthRequest(String uri) {
        if (uri == null) return false;
        return uri.startsWith("/api/v1/auth/")
            || uri.startsWith("/api/auth/")
            || uri.startsWith("/auth/")
            || uri.startsWith("/oauth2/")
            || uri.startsWith("/login/oauth2/");
    }

    private void block(HttpServletResponse response, String ip, boolean auth) throws IOException {
        logger.warn("[RATE LIMIT] IP blocked ({}): {}", auth ? "sign-in" : "general", ip);
        response.setStatus(429);
        response.setHeader("Retry-After", "60");
        response.setHeader("Content-Type", "application/json");
        response.getWriter().write(
            "{\"error\": \"Too many requests. Try again in 60 seconds.\"}");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
