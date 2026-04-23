package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Integer> requests = new HashMap<>();
    private final int LIMIT = 50;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String ip = req.getRemoteAddr();

        requests.put(ip, requests.getOrDefault(ip, 0) + 1);

        if (requests.get(ip) > LIMIT) {
            res.setStatus(429);
            res.getWriter().write("Too many requests");
            return;
        }

        chain.doFilter(req, res);
    }
}
