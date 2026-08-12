package com.vsu.researchapp.infrastructure.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IpBlockFilter extends OncePerRequestFilter {

    private final Map<String, Long> blocked = new HashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String ip = req.getRemoteAddr();

        if (blocked.containsKey(ip)) {
            if (System.currentTimeMillis() < blocked.get(ip)) {
                res.setStatus(403);
                res.getWriter().write("IP blocked");
                return;
            } else {
                blocked.remove(ip);
            }
        }

        chain.doFilter(req, res);
    }
}
