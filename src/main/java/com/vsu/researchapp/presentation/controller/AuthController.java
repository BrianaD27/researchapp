package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.dto.ForgotPasswordRequest;
import com.vsu.researchapp.application.dto.LoginRequest;
import com.vsu.researchapp.application.dto.LogoutRequest;
import com.vsu.researchapp.application.dto.RefreshTokenRequest;
import com.vsu.researchapp.application.dto.RegisterRequest;
import com.vsu.researchapp.application.dto.ResetPasswordRequest;
import com.vsu.researchapp.application.dto.Verify2FARequest;
import com.vsu.researchapp.application.service.PartnerSchoolService;
import com.vsu.researchapp.application.service.UserAccountService;
import com.vsu.researchapp.infrastructure.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final TokenBlacklistService tokenBlacklistService;
    private final PartnerSchoolService partnerSchoolService;

    public AuthController(UserAccountService userAccountService,
            TokenBlacklistService tokenBlacklistService,
            PartnerSchoolService partnerSchoolService) {
        this.partnerSchoolService = partnerSchoolService;
        this.userAccountService = userAccountService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        Map<String, String> result = userAccountService.login(
            request.username(), request.password(), ip, userAgent);

        if ("2FA_REQUIRED".equals(result.get("status"))) {
            return ResponseEntity.ok(Map.of(
                "status", "2FA_REQUIRED",
                "message", "Check your email for a verification code"
            ));
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
            userAccountService.refreshAccessToken(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest) {

        // Only a logged-in user can log out, and only themselves. The username in
        // the request body is ignored; it used to let anyone end anyone's session.
        var auth = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        String authHeader = httpRequest.getHeader("Authorization");
        if (auth == null || !auth.isAuthenticated()
                || auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken
                || authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
        }

        tokenBlacklistService.blacklist(authHeader.substring(7), 86400000);
        userAccountService.logout(auth.getName());
        return ResponseEntity.ok(Map.of(
            "message", "Logged out successfully"
        ));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(
            @Valid @RequestBody Verify2FARequest request) {
        String token = userAccountService.verify2FA(request.username(), request.code());
        return ResponseEntity.ok(Map.of(
            "token", token,
            "type", "Bearer"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {
        // Public sign-up always creates a student account. Any role sent in the
        // request is ignored; faculty accounts are created or promoted by an admin.
        // VSU emails get a student account. Emails from an active partner school
        // get a view-only PARTNER account. Everything else is refused.
        boolean vsu = PartnerSchoolService.isVsuEmail(request.email());
        if (!vsu) partnerSchoolService.requireSchoolFor(request.email());
        var user = vsu
            ? userAccountService.createUser(
                request.username(), request.email(), request.password(), "STUDENT")
            : userAccountService.createPartnerUser(
                request.username(), request.email(), request.password());
        // The account stays inactive until the emailed link is clicked.
        userAccountService.startEmailVerification(user);
        return ResponseEntity.ok(Map.of(
            "message", "Account created. Check your email for a link to activate it.",
            "username", user.getUsername()
        ));
    }

    public record VerifyEmailRequest(String token) {}
    public record ResendVerificationRequest(String usernameOrEmail) {}

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        userAccountService.verifyEmail(request.token());
        return ResponseEntity.ok(Map.of("message", "Email verified. You can log in now."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) {
        userAccountService.resendEmailVerification(request.usernameOrEmail());
        return ResponseEntity.ok(Map.of("message",
            "If that account is waiting for activation, a new link is on its way."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        userAccountService.generatePasswordResetToken(request.username());
        return ResponseEntity.ok(Map.of(
            "message", "If an account exists for this user, a password reset email has been sent"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userAccountService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of(
            "message", "Password reset successfully"
        ));
    }

    @GetMapping("/preview-login")
    public String previewLogin() {
        return "login";
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
