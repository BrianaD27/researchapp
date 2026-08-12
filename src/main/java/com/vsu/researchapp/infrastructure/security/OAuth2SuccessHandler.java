package com.vsu.researchapp.infrastructure.security;

import com.vsu.researchapp.application.service.UserAccountService;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger =
        LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final JwtUtil jwtUtil;
    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;

    public OAuth2SuccessHandler(JwtUtil jwtUtil,
            UserAccountRepository userAccountRepository,
            UserAccountService userAccountService) {
        this.jwtUtil = jwtUtil;
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();

        // Auto-create account if first time logging in with VSU SSO
        Optional<UserAccount> existing =
            userAccountRepository.findByEmail(email);

        UserAccount user;
        if (existing.isEmpty()) {
            logger.info("[OAUTH2] New VSU user logging in: {}", email);
            user = new UserAccount();
            user.setUsername(email);
            user.setEmail(email);
            user.setPasswordHash("SSO_USER");
            user.setActive(true);
            user.setAccountLocked(false);
            user.setFailedAttempts(0);
            user.setTwoFactorEnabled(false);

            // Auto-assign role based on email
            if (email.endsWith("@vsu.edu")) {
                user.setRole("ROLE_STUDENT");
            } else {
                user.setRole("ROLE_STUDENT");
            }

            userAccountRepository.save(user);
        } else {
            user = existing.get();
        }

        // Generate JWT and redirect to frontend with token
        String token = jwtUtil.generateToken(
            user.getUsername(), user.getRole());

        String redirectUrl = "http://localhost:3000/auth/callback?token=" 
            + token;

        logger.info("[OAUTH2] VSU SSO login success: {}", email);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
