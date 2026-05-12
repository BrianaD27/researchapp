package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import com.vsu.researchapp.infrastructure.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserAccountService(UserAccountRepository userAccountRepository,
                              PasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // 1) Create a new user (used by /register)
    public UserAccount createUser(String username, String email, String password, String role) {
        validatePasswordStrength(password);

        UserAccount existing = userAccountRepository.findByUsername(username).orElse(null);
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

    // 2) Login - returns JWT token on success
    public String login(String username, String rawPassword) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is inactive");
        }

        handleAutoUnlockIfNeeded(user);

        if (user.isAccountLocked()) {
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
            throw new RuntimeException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());

        if (user.isTwoFactorEnabled()) {
            String code = generate2FACode();
            user.setTwoFactorCode(code);
            user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));
            userAccountRepository.save(user);
            return "2FA_REQUIRED";
        }

        userAccountRepository.save(user);
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    // 3) Verify 2FA code
    public String verify2FA(String username, String code) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isTwoFactorEnabled()) {
            return "2FA_NOT_ENABLED";
        }

        if (user.getTwoFactorCode() == null || user.getTwoFactorExpiry() == null) {
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

    // 4) Enable 2FA
    public UserAccount enableTwoFactor(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(true);
        return userAccountRepository.save(user);
    }

    // 5) Disable 2FA
    public UserAccount disableTwoFactor(String username) {
        UserAccount user = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTwoFactorEnabled(false);
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        return userAccountRepository.save(user);
    }

    // 6) Change password with history check
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

    // 7) Get all users
    public List<UserAccount> findAll() {
        return userAccountRepository.findAll();
    }

    // 8) Get one user by id
    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException(
                "Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new RuntimeException(
                "Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new RuntimeException(
                "Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new RuntimeException(
                "Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new RuntimeException(
                "Password must contain at least one special character");
        }
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
