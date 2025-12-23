package com.vsu.researchapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // For now (dev/testing): disable CSRF so POST/PUT work easily in browser/tests.
            .csrf(csrf -> csrf.disable())

            // Use sessions for browser login pages (you have login.html/dashboard.html)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            .authorizeHttpRequests(auth -> auth

                // Allow health + swagger if you have them
                .requestMatchers("/actuator/health", "/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Allow login + static assets if you have them
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                // IMPORTANT: allow Professor API without authentication
                .requestMatchers("/api/professors/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Admin zone
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else under /api requires a logged-in user
                .requestMatchers("/api/**").authenticated()

                // Non-api paths: keep open (or lock later)
                .anyRequest().permitAll()
            )

            //  THIS removes the browser popup (Basic Auth prompt)
            .httpBasic(basic -> basic.disable())

            // Keep form login (so browser redirects to login page instead of popup)
            .formLogin(form -> form.permitAll());

        return http.build();
    }
}

