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
            // --- H2 Console requires: allow frames + don't CSRF-block its POST ---
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

            // --- Session hardening ---
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(session -> session.migrateSession())
            )

            // --- Authorization rules ---
            .authorizeHttpRequests(auth -> auth
                // Health + swagger (optional)
                .requestMatchers("/actuator/health", "/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // H2 console
                .requestMatchers("/h2-console/**").permitAll()

                // Auth pages + static files
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                // CORS preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public API (keep if you want public read access)
                .requestMatchers("/api/professors/**").permitAll()

                // Admin-only
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Everything else requires login
                .anyRequest().authenticated()
            )

            // Disable browser popup basic auth
            .httpBasic(basic -> basic.disable())

            // --- Logout hardening ---
            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout")
            )

            // --- Form login + lockout handling hooks ---
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
