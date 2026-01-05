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

    private final LoginSecurityService loginSecurityService;

    public SecurityConfig(LoginSecurityService loginSecurityService) {
        this.loginSecurityService = loginSecurityService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // Dev mode: CSRF off (OK for now)
            .csrf(csrf -> csrf.disable())

            //  Security Headers
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(contentType -> {})
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "img-src 'self' data:; " +
                        "script-src 'self'; " +
                        "style-src 'self' 'unsafe-inline'"
                    )
                )
                .httpStrictTransportSecurity(hsts -> hsts.disable())
            )

            //  Session hardening
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(session -> session.migrateSession())
            )

            .authorizeHttpRequests(auth -> auth

                // Health + actuator
                .requestMatchers("/actuator/health", "/health").permitAll()

                // Swagger (if enabled)
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Auth pages + static files
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                // Public API
                .requestMatchers("/api/professors/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Admin-only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                //  All other API calls require login
                .requestMatchers("/api/**").authenticated()

                //  UI pages that REQUIRE login
                .requestMatchers("/dashboard", "/home").authenticated()

                // Everything else open for now
                .anyRequest().permitAll()
            )

            // Disable browser basic auth popup
            .httpBasic(basic -> basic.disable())

            //  Logout hardening
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout")
            )

            //  Login handling
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    String username = authentication.getName();
                    loginSecurityService.onLoginSuccess(username);
                    response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    if (username != null && !username.isBlank()) {
                        loginSecurityService.onLoginFailure(username);
                    }

                    String redirect = "/login?error";
                    if (exception instanceof org.springframework.security.authentication.LockedException) {
                        redirect = "/login?locked";
                    } else if (exception instanceof org.springframework.security.authentication.DisabledException) {
                        redirect = "/login?disabled";
                    }

                    response.sendRedirect(redirect);
                })
            );

        return http.build();
    }
}
