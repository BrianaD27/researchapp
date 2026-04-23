package com.vsu.researchapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    @Bean
    public IpAllowlistFilter ipAllowlistFilter() {
        return new IpAllowlistFilter();
    }

    @Bean
    public AuditLogFilter auditLogFilter() {
        return new AuditLogFilter();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Disable CSRF for API
            .csrf(csrf -> csrf.disable())

            // Enable CORS (quick win)
            .cors(Customizer.withDefaults())

            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**",
                    "/api/auth/**",
                    "/h2-console/**"
                ).permitAll()

                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/professors/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers("/api/students/**").hasAnyRole("ADMIN", "PROFESSOR")

                .requestMatchers("/api/research-events/**").authenticated()
                .requestMatchers("/api/encrypted-files/**").authenticated()

                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )

            // Filters (order matters)
            .addFilterBefore(ipAllowlistFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(auditLogFilter(), UsernamePasswordAuthenticationFilter.class)

            // Basic auth for now
            .httpBasic(Customizer.withDefaults())

            // Security headers (important)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'")
                )
                .referrerPolicy(referrer ->
                    referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
                )
            );

        return http.build();
    }
}
