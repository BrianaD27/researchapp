package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        Map<String, String> result = userAccountService.login(
            username, password, ip, userAgent);

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
            @RequestParam String refreshToken) {
        return ResponseEntity.ok(
            userAccountService.refreshAccessToken(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestParam String username) {
        userAccountService.logout(username);
        return ResponseEntity.ok(Map.of(
            "message", "Logged out successfully"
        ));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(
            @RequestParam String username,
            @RequestParam String code) {
        String token = userAccountService.verify2FA(username, code);
        return ResponseEntity.ok(Map.of(
            "token", token,
            "type", "Bearer"
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "STUDENT") String role) {
        var user = userAccountService.createUser(
            username, email, password, role);
        return ResponseEntity.ok(Map.of(
            "message", "User created successfully",
            "username", user.getUsername()
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @RequestParam String username) {
        String token = userAccountService
            .generatePasswordResetToken(username);
        return ResponseEntity.ok(Map.of(
            "message", "Password reset token generated",
            "token", token
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        userAccountService.resetPassword(token, newPassword);
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
