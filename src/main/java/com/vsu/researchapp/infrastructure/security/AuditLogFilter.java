package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class AuditLogFilter extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(AuditLogFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ip = getClientIp(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String user = request.getRemoteUser() != null ?
            request.getRemoteUser() : "anonymous";

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();

            if (status == 401 || status == 403) {
                logger.warn("[AUDIT] {} | IP: {} | User: {} | {} {} | Status: {} | {}ms",
                    LocalDateTime.now(), ip, user, method, uri, status, duration);
            } else {
                logger.info("[AUDIT] {} | IP: {} | User: {} | {} {} | Status: {} | {}ms",
                    LocalDateTime.now(), ip, user, method, uri, status, duration);
            }
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
