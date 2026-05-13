package com.vsu.researchapp.presentation.controller;

import com.vsu.researchapp.application.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        String result = userAccountService.login(
            username, password, ip, userAgent);

        if ("2FA_REQUIRED".equals(result)) {
            return ResponseEntity.ok(Map.of(
                "status", "2FA_REQUIRED",
                "message", "Check your email for a verification code"
            ));
        }

        return ResponseEntity.ok(Map.of(
            "token", result,
            "type", "Bearer"
        ));
    }

    // POST /api/auth/verify-2fa
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

    // POST /api/auth/register
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

    // GET /api/auth/preview-login
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
