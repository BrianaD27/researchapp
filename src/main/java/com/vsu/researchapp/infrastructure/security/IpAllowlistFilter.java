package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

public class IpAllowlistFilter extends OncePerRequestFilter {

    private static final Set<String> ALLOWED_IPS = Set.of(
        "127.0.0.1",
        "0:0:0:0:0:0:0:1"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = request.getRemoteAddr();

        if (path.startsWith("/auth") || path.startsWith("/api/auth") || path.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!ALLOWED_IPS.contains(ip)) {
            response.setStatus(403);
            response.getWriter().write("Access denied from this IP");
            return;
        }

        filterChain.doFilter(request, response);
    }
}