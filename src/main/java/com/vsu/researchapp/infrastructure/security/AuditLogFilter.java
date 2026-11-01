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
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();
            // Read the user after the request ran: the login check comes later in
            // the chain, so before this point every request looked anonymous.
            String user = currentUser();

            if (status == 401 || status == 403) {
                logger.warn("[AUDIT] {} | IP: {} | User: {} | {} {} | Status: {} | {}ms",
                    LocalDateTime.now(), ip, user, method, uri, status, duration);
            } else {
                logger.info("[AUDIT] {} | IP: {} | User: {} | {} {} | Status: {} | {}ms",
                    LocalDateTime.now(), ip, user, method, uri, status, duration);
            }
        }
    }

    private String currentUser() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return auth.getName();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
