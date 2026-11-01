package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.LoginHistory;
import com.vsu.researchapp.domain.model.PasswordResetToken;
import com.vsu.researchapp.domain.model.RefreshToken;
import com.vsu.researchapp.domain.model.UserAccount;
import com.vsu.researchapp.domain.repositoryinterfaces.UserAccountRepository;

import com.vsu.researchapp.domain.repositoryinterfaces.LoginHistoryRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.PasswordResetTokenRepository;
import com.vsu.researchapp.domain.repositoryinterfaces.EmailVerificationTokenRepository;
import com.vsu.researchapp.domain.model.EmailVerificationToken;
import com.vsu.researchapp.domain.repositoryinterfaces.RefreshTokenRepository;
import com.vsu.researchapp.infrastructure.externalServices.email.emailService;
import com.vsu.researchapp.infrastructure.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccountService {

    private static final Logger logger =
        LoggerFactory.getLogger(UserAccountService.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final emailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    public UserAccountService(UserAccountRepository userAccountRepository,
                              PasswordEncoder passwordEncoder,
                              JwtUtil jwtUtil,
                              LoginHistoryRepository loginHistoryRepository,
                              PasswordResetTokenRepository passwordResetTokenRepository,
                              RefreshTokenRepository refreshTokenRepository,
                              emailService emailService,
                              EmailVerificationTokenRepository emailVerificationTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.loginHistoryRepository = loginHistoryRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
    }

    public UserAccount createUser(String username, String email,
            String password, String role) {
        validatePasswordStrength(password);
        validateVsuEmail(email);

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

        // Roles are stored WITHOUT the "ROLE_" prefix (e.g. "STUDENT", not
        // "ROLE_STUDENT"). JwtFilter is the only place that adds "ROLE_", at the
        // moment it grants the Spring Security authority for an incoming request.
        // This method used to add the prefix here too, which meant every
        // self-registered account ended up with a doubled authority like
        // "ROLE_ROLE_STUDENT" - that string matches no hasRole()/hasAnyRole() check,
        // so self-registered users were silently locked out of every role-gated
        // endpoint (including the ones meant for them, like creating their own
        // student profile).
        String normalizedRole = (role == null || role.isBlank())
            ? "STUDENT"
            : role.trim().toUpperCase().replaceFirst("^ROLE_", "");

        // Anyone can call this method's caller endpoints (/api/v1/auth/register and
        // /api/v1/users/register) while only authenticated (not admin-gated). Without
        // this check, any logged-in user - even a STUDENT - could register a brand
        // new ADMIN account for themselves. Public self-registration is limited to
        // the two roles a signup form is actually meant to hand out.
        if (!normalizedRole.equals("STUDENT") && !normalizedRole.equals("PROFESSOR")) {
            throw new IllegalArgumentException(
                "Self-registration only supports the STUDENT or PROFESSOR role");
        }

        user.setRole(normalizedRole);

        user.setActive(true);
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        user.setTwoFactorEnabled(false);

        UserAccount saved = userAccountRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcome(email, username);
        } catch (Exception e) {
            logger.warn("[EMAIL] Could not send welcome email: {}",
                e.getMessage());
        }

        return saved;
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
            try {
                emailService.sendAccountLocked(user.getEmail());
            } catch (Exception e) {
                logger.warn("[EMAIL] Could not send lock notification: {}",
                    e.getMessage());
            }
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

        // Checked only after the right password, so this can't be used to find
        // out which accounts exist.
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            user.setFailedAttempts(0);
            userAccountRepository.save(user);
            saveLoginHistory(username, ip, userAgent, "UNVERIFIED");
            throw new RuntimeException("Email not verified");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());

        if (user.isTwoFactorEnabled()) {
            String code = generate2FACode();
            user.setTwoFactorCode(code);
            user.setTwoFactorExpiry(LocalDateTime.now().plusMinutes(5));
            userAccountRepository.save(user);
            saveLoginHistory(username, ip, userAgent, "2FA_REQUIRED");

            // Send 2FA code via email
            try {
                emailService.send2FACode(user.getEmail(), code);
            } catch (Exception e) {
                logger.warn("[EMAIL] Could not send 2FA code: {}",
                    e.getMessage());
            }

            return java.util.Map.of("status", "2FA_REQUIRED");
        }

        userAccountRepository.save(user);
        saveLoginHistory(username, ip, userAgent, "SUCCESS");

        // Send login alert
        try {
            emailService.sendLoginAlert(user.getEmail(), ip, userAgent);
        } catch (Exception e) {
            logger.warn("[EMAIL] Could not send login alert: {}",
                e.getMessage());
        }

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
        if (!user.isActive()) {
            refreshTokenRepository.delete(stored);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Account is no longer active");
        }

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

    public Page<UserAccount> findAllPaginated(Pageable pageable) {
        return userAccountRepository.findAll(pageable);
    }

    public Optional<UserAccount> findById(Long id) {
        return userAccountRepository.findById(id);
    }

    public List<LoginHistory> getLoginHistory(String username) {
        return loginHistoryRepository
            .findTop10ByUsernameOrderByLoginTimeDesc(username);
    }

    // Public sign-up: the account can't log in until the emailed link is clicked,
    // which proves the person owns that VSU address.
    public void startEmailVerification(UserAccount user) {
        user.setEmailVerified(false);
        userAccountRepository.save(user);
        issueVerificationLink(user);
    }

    public void verifyEmail(String token) {
        EmailVerificationToken stored = emailVerificationTokenRepository
            .findByTokenHash(hashResetToken(token == null ? "" : token.trim()))
            .orElseThrow(() -> new RuntimeException("Invalid activation link"));
        if (stored.isUsed()) {
            throw new RuntimeException("This activation link was already used");
        }
        if (stored.getExpiresAt() == null || LocalDateTime.now().isAfter(stored.getExpiresAt())) {
            throw new RuntimeException("This activation link expired");
        }
        UserAccount user = userAccountRepository.findByUsername(stored.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid activation link"));
        user.setEmailVerified(true);
        userAccountRepository.save(user);
        stored.setUsed(true);
        emailVerificationTokenRepository.save(stored);
        logger.info("[VERIFY] Email verified for {}", user.getUsername());
    }

    // Same response whether or not the account exists or needs it; at most one
    // new link every 2 minutes per account.
    public void resendEmailVerification(String usernameOrEmail) {
        String identifier = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        Optional<UserAccount> found = userAccountRepository.findByUsername(identifier);
        if (found.isEmpty() && identifier.contains("@")) {
            List<UserAccount> matches = userAccountRepository.findAllByEmailIgnoreCase(identifier);
            if (matches.size() == 1) {
                found = Optional.of(matches.get(0));
            }
        }
        if (found.isEmpty() || !Boolean.FALSE.equals(found.get().getEmailVerified())) {
            return;
        }
        UserAccount user = found.get();
        Optional<EmailVerificationToken> recent =
            emailVerificationTokenRepository.findFirstByUsernameOrderByIdDesc(user.getUsername());
        if (recent.isPresent() && recent.get().getCreatedAt() != null
                && recent.get().getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(2))) {
            logger.info("[VERIFY] Ignoring repeat activation request within two minutes");
            return;
        }
        issueVerificationLink(user);
    }

    private void issueVerificationLink(UserAccount user) {
        emailVerificationTokenRepository.deleteByUsername(user.getUsername());
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        EmailVerificationToken stored = new EmailVerificationToken();
        stored.setTokenHash(hashResetToken(token));
        stored.setUsername(user.getUsername());
        emailVerificationTokenRepository.save(stored);
        String email = user.getEmail();
        String username = user.getUsername();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmailVerification(email, username, token);
            } catch (Exception e) {
                logger.warn("[EMAIL] Could not send activation email: {}", e.getMessage());
            }
        });
    }

    // Accepts a username or the account's email. Unknown accounts return silently
    // so the caller always gets the same response and cannot tell which accounts
    // exist.
    public void generatePasswordResetToken(String usernameOrEmail) {
        String identifier = usernameOrEmail == null ? "" : usernameOrEmail.trim();
        Optional<UserAccount> found = userAccountRepository.findByUsername(identifier);
        if (found.isEmpty() && identifier.contains("@")) {
            List<UserAccount> matches = userAccountRepository.findAllByEmailIgnoreCase(identifier);
            if (matches.size() == 1) {
                found = Optional.of(matches.get(0));
            }
        }
        if (found.isEmpty()) {
            logger.info("[RESET] Password reset requested for an unknown account");
            return;
        }

        UserAccount user = found.get();
        String username = user.getUsername();

        // At most one reset email per account per minute.
        Optional<PasswordResetToken> recent =
            passwordResetTokenRepository.findFirstByUsernameOrderByIdDesc(username);
        if (recent.isPresent() && !recent.get().isUsed()
                && recent.get().getExpiresAt() != null
                && recent.get().getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(14))) {
            logger.info("[RESET] Ignoring repeat reset request within one minute");
            return;
        }

        passwordResetTokenRepository.deleteByUsername(username);

        String token = UUID.randomUUID().toString() +
            UUID.randomUUID().toString();

        // Only a SHA-256 hash of the token is stored; the raw token exists only in
        // the emailed link.
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(hashResetToken(token));
        resetToken.setUsername(username);
        passwordResetTokenRepository.save(resetToken);

        // Sent in the background so known and unknown accounts respond in similar time.
        String email = user.getEmail();
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendPasswordReset(email, token);
            } catch (Exception e) {
                logger.warn("[EMAIL] Could not send password reset email: {}",
                    e.getMessage());
            }
        });
    }

    private static String hashResetToken(String token) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public void resetPassword(String token, String newPassword) {
        validatePasswordStrength(newPassword);

        PasswordResetToken resetToken = passwordResetTokenRepository
            .findByToken(hashResetToken(token == null ? "" : token.trim()))
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
        // Proving control of the account's email also clears a lockout.
        user.setFailedAttempts(0);
        user.setAccountLocked(false);
        userAccountRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Sign the account out of every device that holds a refresh token.
        refreshTokenRepository.deleteByUsername(user.getUsername());
    }

    public UserAccount assignRole(String username, String role) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        // Store roles without the "ROLE_" prefix, matching createUser(); JwtFilter
        // adds the prefix. Storing "ROLE_X" here produced "ROLE_ROLE_X" authorities
        // that matched no role check, so promoted accounts lost access.
        String normalizedRole = role == null ? "" : role.trim().toUpperCase().replaceFirst("^ROLE_", "");
        if (!java.util.Set.of("STUDENT", "PROFESSOR", "ASSOCIATE", "ADMIN").contains(normalizedRole)) {
            throw new IllegalArgumentException("Role must be STUDENT, PROFESSOR, ASSOCIATE, or ADMIN");
        }
        user.setRole(normalizedRole);
        return userAccountRepository.save(user);
    }

    public UserAccount setActiveStatus(String username, boolean active) {
        UserAccount user = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(active);
        return userAccountRepository.save(user);
    }

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
        // Any symbol or space counts as a special character, not just a fixed list.
        if (password.chars().allMatch(Character::isLetterOrDigit))
            throw new RuntimeException("Password must contain at least one special character or space");
        // BCrypt only uses the first 72 bytes; reject longer passwords clearly
        // instead of silently ignoring the rest.
        if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new RuntimeException("Password must be 72 bytes or fewer (about 72 standard characters)");
    }

    // Registration is limited to VSU accounts: faculty/staff (@vsu.edu) and
    // students (@students.vsu.edu). Enforced here as well as via @Pattern on
    // RegisterRequest so every caller of createUser() is covered.
    private static final java.util.regex.Pattern VSU_EMAIL_PATTERN =
        java.util.regex.Pattern.compile(
            "^[A-Za-z0-9._%+-]+@(students\\.)?vsu\\.edu$",
            java.util.regex.Pattern.CASE_INSENSITIVE);

    private void validateVsuEmail(String email) {
        if (email == null || !VSU_EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException(
                "Email must be a valid VSU address ending in @vsu.edu or @students.vsu.edu");
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
