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
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class SqlInjectionFilter extends OncePerRequestFilter {

    private static final Logger logger =
        LoggerFactory.getLogger(SqlInjectionFilter.class);

    private static final Pattern SQL_PATTERN = Pattern.compile(
        ".*([';]+|(--)+|(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|" +
        "CREATE|ALTER|EXEC|UNION|FETCH|DECLARE|CAST|CONVERT|" +
        "TRUNCATE|BACKUP|RESTORE)\\b)).*",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        Map<String, String[]> params = request.getParameterMap();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            for (String value : entry.getValue()) {
                if (value != null && SQL_PATTERN.matcher(value).matches()) {
                    logger.warn("[SQL INJECTION] Detected from IP: {} " +
                        "Param: {} Value: {}",
                        request.getRemoteAddr(),
                        entry.getKey(),
                        value.substring(0, Math.min(50, value.length())));

                    response.setStatus(400);
                    response.setHeader("Content-Type", "application/json");
                    response.getWriter().write(
                        "{\"error\": \"Invalid input detected\"}");
                    return;
                }
            }
        }

        chain.doFilter(request, response);
    }
}
