package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.dto.ForgotPasswordRequest;
import com.vsu.researchapp.application.dto.LoginRequest;
import com.vsu.researchapp.application.dto.LogoutRequest;
import com.vsu.researchapp.application.dto.RefreshTokenRequest;
import com.vsu.researchapp.application.dto.RegisterRequest;
import com.vsu.researchapp.application.dto.ResetPasswordRequest;
import com.vsu.researchapp.application.dto.Verify2FARequest;
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

    public AuthController(UserAccountService userAccountService,
            TokenBlacklistService tokenBlacklistService) {
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
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklist(token, 86400000);
        }

        userAccountService.logout(request.username());
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
        String role = request.role() == null || request.role().isBlank()
            ? "STUDENT" : request.role();
        var user = userAccountService.createUser(
            request.username(), request.email(), request.password(), role);
        return ResponseEntity.ok(Map.of(
            "message", "User created successfully",
            "username", user.getUsername()
        ));
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
