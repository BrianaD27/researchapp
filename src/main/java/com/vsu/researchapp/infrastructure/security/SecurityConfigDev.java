package com.vsu.researchapp.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@Profile("dev")
public class SecurityConfigDev {

    private final LoginSecurityService loginSecurityService;

    public SecurityConfigDev(LoginSecurityService loginSecurityService) {
        this.loginSecurityService = loginSecurityService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // DEV: CSRF OFF (Postman / Swagger friendly)
            .csrf(csrf -> csrf.disable())

            .headers(headers ->
                headers.frameOptions(frame -> frame.sameOrigin())
            )

            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation(session -> session.migrateSession())
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/login", "/logout").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/professors/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .httpBasic(basic -> basic.disable())

            .logout(logout -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessUrl("/login?logout")
            )

            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    loginSecurityService.onLoginSuccess(authentication.getName());
                    response.sendRedirect("/dashboard");
                })
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    if (username != null && !username.isBlank()) {
                        loginSecurityService.onLoginFailure(username);
                    }
                    response.sendRedirect("/login?error");
                })
            );

        return http.build();
    }
}
