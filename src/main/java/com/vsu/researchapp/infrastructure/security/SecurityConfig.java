package com.vsu.researchapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    private final IpAllowlistFilter ipAllowlistFilter;
    private final AuditLogFilter auditLogFilter;
    private final OAuth2SuccessHandler oauth2SuccessHandler;
    private final RedisTemplate<String, Object> redisTemplate;

    public SecurityConfig(JwtFilter jwtFilter,
                          IpAllowlistFilter ipAllowlistFilter,
                          AuditLogFilter auditLogFilter,
                          OAuth2SuccessHandler oauth2SuccessHandler,
                          RedisTemplate<String, Object> redisTemplate) {
        this.jwtFilter = jwtFilter;
        this.ipAllowlistFilter = ipAllowlistFilter;
        this.auditLogFilter = auditLogFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter(redisTemplate);
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
                // Uploaded media (profile pictures, research media) is served as static
                // content and must be reachable by unauthenticated <img>/<a> tags.
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers(
                    "/api/v1/admin/**",
                    "/admin/**",
                    "/api/admin/**").hasRole("ADMIN")
                // Browsing (GET) is open to any authenticated role: students need to
                // browse professors/opportunities, and professors need to browse
                // students to recruit - that's the core purpose of the app. Writes
                // are role-gated to whoever the resource is "about" (STUDENT can only
                // write /api/students, PROFESSOR can only write /api/professors),
                // with ADMIN always allowed. Which SPECIFIC record a non-admin can
                // write to (only their own) is enforced in StudentService/
                // ProfessorService, since a path-based rule here can only say "you're
                // some student," not "you're THIS student."
                .requestMatchers(HttpMethod.GET,
                    "/api/v1/professors/**",
                    "/api/professors/**").hasAnyRole("ADMIN", "PROFESSOR", "STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/professors/**", "/api/professors/**")
                    .hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT, "/api/v1/professors/**", "/api/professors/**")
                    .hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/professors/**", "/api/professors/**")
                    .hasAnyRole("ADMIN", "PROFESSOR")

                .requestMatchers(HttpMethod.GET,
                    "/api/v1/students/**",
                    "/api/students/**").hasAnyRole("ADMIN", "PROFESSOR", "STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/v1/students/**", "/api/students/**")
                    .hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers(HttpMethod.PUT, "/api/v1/students/**", "/api/students/**")
                    .hasAnyRole("ADMIN", "STUDENT")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/students/**", "/api/students/**")
                    .hasAnyRole("ADMIN", "STUDENT")
                // Research media mutations are restricted to the owning professor (or an
                // admin); ownership of the specific opportunity is enforced in
                // MediaUploadService. GET (listing/browsing media) stays open to any
                // authenticated role via anyRequest() below.
                .requestMatchers(HttpMethod.POST,
                    "/api/v1/research-opportunities/*/media",
                    "/api/research-opportunities/*/media").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.PUT,
                    "/api/v1/research-opportunities/*/media",
                    "/api/research-opportunities/*/media").hasAnyRole("ADMIN", "PROFESSOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/api/v1/research-opportunities/*/media",
                    "/api/research-opportunities/*/media").hasAnyRole("ADMIN", "PROFESSOR")
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

            // Without this, Spring Security's default reaction to a failed/missing
            // authentication on ANY request is to redirect toward the oauth2Login flow
            // (since one is registered), instead of returning 401. That breaks every
            // stateless JWT API caller (Swagger, curl, a frontend fetch) with a redirect
            // to a page they can't follow. API requests should get a plain 401; only the
            // OAuth2 endpoints below are login-flow related.
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    (request, response, authException) ->
                        response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"),
                    request -> !request.getRequestURI().startsWith("/oauth2/")
                        && !request.getRequestURI().startsWith("/login/oauth2/")
                )
            )

            .oauth2Login(oauth2 -> oauth2
                .successHandler(oauth2SuccessHandler)
                .failureUrl("/api/v1/auth/login?error=oauth2")
            )

            .addFilterBefore(ipAllowlistFilter,
                UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(rateLimitFilter(),
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