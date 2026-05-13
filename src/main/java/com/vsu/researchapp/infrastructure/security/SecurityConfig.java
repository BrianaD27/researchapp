package com.vsu.researchapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;
    private final IpAllowlistFilter ipAllowlistFilter;
    private final AuditLogFilter auditLogFilter;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    public SecurityConfig(JwtFilter jwtFilter,
                          RateLimitFilter rateLimitFilter,
                          IpAllowlistFilter ipAllowlistFilter,
                          AuditLogFilter auditLogFilter,
                          OAuth2SuccessHandler oauth2SuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.ipAllowlistFilter = ipAllowlistFilter;
        this.auditLogFilter = auditLogFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "https://researchapp.vsu.edu"
        ));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/auth/**",
                    "/api/auth/**",
                    "/login/oauth2/**",
                    "/oauth2/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(
                    "/api/v1/admin/**",
                    "/admin/**",
                    "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(
                    "/api/v1/professors/**",
                    "/api/professors/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(
                    "/api/v1/students/**",
                    "/api/students/**").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(
                    "/api/v1/research-events/**",
                    "/api/research-events/**").authenticated()
                .requestMatchers(
                    "/api/v1/encrypted-files/**",
                    "/api/encrypted-files/**").authenticated()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").denyAll()
                .anyRequest().authenticated()
            )

            // Microsoft OAuth2 SSO
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler)
                .failureUrl("/api/v1/auth/login?error=oauth2")
            )

            .addFilterBefore(ipAllowlistFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(auditLogFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class)

            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .contentSecurityPolicy(csp ->
                    csp.policyDirectives("default-src 'self'"))
                .referrerPolicy(referrer ->
                    referrer.policy(
                        org.springframework.security.web.header.writers
                        .ReferrerPolicyHeaderWriter.ReferrerPolicy
                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
            );

        return http.build();
    }
}
