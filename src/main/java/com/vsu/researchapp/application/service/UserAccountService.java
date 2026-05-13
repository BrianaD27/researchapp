package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.LoginHistory;
import com.vsu.researchapp.domain.model.PasswordResetToken;
import com.vsu.researchapp.domain.model.RefreshToken;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.LoginHistoryRepository;
import com.vsu.researchapp.domain.repository.PasswordResetTokenRepository;
import com.vsu.researchapp.domain.repository.RefreshTokenRepository;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import com.vsu.researchapp.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserAccountService(UserAccountRepository userAccountRepository,
                              PasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil,
                              LoginHistoryRepository loginHistoryRepository,
                              PasswordResetTokenRepository passwordResetTokenRepository,
                              RefreshTokenRepository refreshTokenRepository) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public UserAccount createUser(String username, String email,
            String password, String role) {
        validatePasswordStrength(password);

        UserAccount existing = userAccountRepository
            .findByUsername(username).orElse(null);
        if (existing != null) {
            throw new RuntimeException("Username already exists");
        }

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setEmail(email);

        String encodedPassword = passwordEncoder.encode(password);
        user.setPasswordHash(encodedPassword);
        user.getPasswordHistory().add(encodedPassword);

        if (role == null || role.isBlank()) {
            user.setRole("ROLE_USER");
        } else if (role.startsWith("ROLE_")) {
            user.setRole(role);
        } else {
            user.setRole("ROLE_" + role.toUpperCase());
        }

        user.setActive(true);
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setTwoFactorEnabled(false);

        return userAccountRepository.save(user);
    }

    public java.util.Map<String, String> login(String username,
            String rawPassword, String ip, String userAgent) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            saveLoginHistory(username, ip, userAgent, "FAILED");
            throw new RuntimeException("Account is inactive");
        }

        handleAutoUnlockIfNeeded(user);

        if (user.isAccountLocked()) {
            saveLoginHistory(username, ip, userAgent, "LOCKED");
            throw new RuntimeException("Account locked. Try again later.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= 5) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            userAccountRepository.save(user);
            saveLoginHistory(username, ip, userAgent, "FAILED");
            throw new RuntimeException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());

        if (user.isTwoFactorEnabled()) {
            String code = generate2FACode();
            user.setTwoFactorCode(code);
            user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));
            userAccountRepository.save(user);
            saveLoginHistory(username, ip, userAgent, "2FA_REQUIRED");
            return java.util.Map.of("status", "2FA_REQUIRED");
        }

        userAccountRepository.save(user);
        saveLoginHistory(username, ip, userAgent, "SUCCESS");

        String accessToken = jwtUtil.generateToken(
            user.getUsername(), user.getRole());
        String refreshToken = generateRefreshToken(username);

        return java.util.Map.of(
            "token", accessToken,
            "refreshToken", refreshToken,
            "type", "Bearer"
        );
    }

    public java.util.Map<String, String> refreshAccessToken(
            String refreshToken) {
        RefreshToken stored = refreshTokenRepository
            .findByToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (LocalDateTime.now().isAfter(stored.getExpiresAt())) {
            refreshTokenRepository.delete(stored);
            throw new RuntimeException("Refresh token expired");
        }

        UserAccount user = userAccountRepository
            .findByUsername(stored.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(
            user.getUsername(), user.getRole());

        return java.util.Map.of(
            "token", newAccessToken,
            "type", "Bearer"
        );
    }

    public void logout(String username) {
        refreshTokenRepository.deleteByUsername(username);
    }

    public String verify2FA(String username, String code) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled()) return "2FA_NOT_ENABLED";

        if (user.getTwoFactorCode() == null ||
                user.getTwoFactorExpiry() == null) {
            throw new RuntimeException("No active 2FA code");
        }

        if (LocalDateTime.now().isAfter(user.getTwoFactorExpiry())) {
            user.setTwoFactorCode(null);
            user.setTwoFactorExpiry(null);
            userAccountRepository.save(user);
            throw new RuntimeException("2FA code expired");
        }

        if (!user.getTwoFactorCode().equals(code)) {
            throw new RuntimeException("Invalid 2FA code");
        }

        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        user.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(user);
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    public UserAccount enableTwoFactor(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(true);
        return userAccountRepository.save(user);
    }

    public UserAccount disableTwoFactor(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(false);
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        return userAccountRepository.save(user);
    }

    public UserAccount changePassword(String username, String newPassword) {
        validatePasswordStrength(newPassword);
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        for (String oldHash : user.getPasswordHistory()) {
            if (passwordEncoder.matches(newPassword, oldHash)) {
                throw new RuntimeException("Cannot reuse an old password");
            }
        }
        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        user.getPasswordHistory().add(newHash);
        return userAccountRepository.save(user);
    }

    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }

    public List<LoginHistory> getLoginHistory(String username) {
        return loginHistoryRepository
            .findTop10ByUsernameOrderByLoginTimeDesc(username);
    }

    public String generatePasswordResetToken(String username) {
        userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        passwordResetTokenRepository.deleteByUsername(username);

        String token = UUID.randomUUID().toString() +
            UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUsername(username);
        passwordResetTokenRepository.save(resetToken);

        return token;
    }

    public void resetPassword(String token, String newPassword) {
        validatePasswordStrength(newPassword);

        PasswordResetToken resetToken = passwordResetTokenRepository
            .findByToken(token)
            .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Reset token already used");
        }

        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            throw new RuntimeException("Reset token expired");
        }

        UserAccount user = userAccountRepository
            .findByUsername(resetToken.getUsername())
            .orElseThrow(() -> new RuntimeException("User not found"));

        for (String oldHash : user.getPasswordHistory()) {
            if (passwordEncoder.matches(newPassword, oldHash)) {
                throw new RuntimeException("Cannot reuse an old password");
            }
        }

        String newHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newHash);
        user.getPasswordHistory().add(newHash);
        userAccountRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    // Admin — assign role
    public UserAccount assignRole(String username, String role) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }
        user.setRole(role);
        return userAccountRepository.save(user);
    }

    // Admin — activate or deactivate user
    public UserAccount setActiveStatus(String username, boolean active) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        return userAccountRepository.save(user);
    }

    // Admin — unlock account
    public UserAccount unlockAccount(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        user.setLockTime(null);
        return userAccountRepository.save(user);
    }

    private String generateRefreshToken(String username) {
        refreshTokenRepository.deleteByUsername(username);

        String token = UUID.randomUUID().toString() +
            UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUsername(username);
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    private void saveLoginHistory(String username, String ip,
            String userAgent, String status) {
        LoginHistory history = new LoginHistory();
        history.setUsername(username);
        history.setIpAddress(ip);
        history.setUserAgent(userAgent);
        history.setStatus(status);
        loginHistoryRepository.save(history);
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8)
            throw new RuntimeException("Password must be at least 8 characters");
        if (!password.matches(".*[A-Z].*"))
            throw new RuntimeException("Password must contain at least one uppercase letter");
        if (!password.matches(".*[a-z].*"))
            throw new RuntimeException("Password must contain at least one lowercase letter");
        if (!password.matches(".*\\d.*"))
            throw new RuntimeException("Password must contain at least one number");
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"))
            throw new RuntimeException("Password must contain at least one special character");
    }

    private void handleAutoUnlockIfNeeded(UserAccount user) {
        if (user.isAccountLocked() && user.getLockTime() != null) {
            if (LocalDateTime.now().isAfter(
                    user.getLockTime().plusMinutes(15))) {
                user.setAccountLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userAccountRepository.save(user);
            }
        }
    }

    private String generate2FACode() {
        SecureRandom secureRandom = new SecureRandom();
        return String.valueOf(100000 + secureRandom.nextInt(900000));
    }
}
