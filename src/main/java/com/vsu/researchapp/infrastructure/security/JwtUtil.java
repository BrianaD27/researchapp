package com.vsu.researchapp.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger =
        LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final long expirationMs;

    // Access tokens last 30 minutes
    private static final long ACCESS_TOKEN_MS = 30 * 60 * 1000;

    // The fallback in application.properties is public on GitHub. Anyone who knows
    // it can sign their own admin tokens, so it must never protect a real server.
    static final String PUBLIC_DEFAULT_SECRET =
        "vsu-research-app-super-secret-key-change-this-in-production-min-32-chars";

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expirationMs,
            @Value("${spring.profiles.active:}") String activeProfiles) {
        if (PUBLIC_DEFAULT_SECRET.equals(secret)) {
            if (activeProfiles.contains("prod")) {
                throw new IllegalStateException(
                    "JWT_SECRET is not set. Refusing to start in production with the public default secret.");
            }
            logger.warn("[JWT] Using the PUBLIC default secret. Set JWT_SECRET; anyone with the repo can forge logins.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    // Generate access token (30 min)
    public String generateToken(String username, String role) {
        return buildToken(username, role, ACCESS_TOKEN_MS);
    }

    // Generate longer token for specific use cases
    public String generateLongToken(String username, String role) {
        return buildToken(username, role, expirationMs);
    }

    private String buildToken(String username, String role,
            long expMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expMs);

        return Jwts.builder()
            .subject(username)
            .claim("role", role)
            .claim("iat_ms", now.getTime())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(secretKey)
            .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long extractIssuedAt(String token) {
        return parseClaims(token).get("iat_ms", Long.class);
    }

    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseClaims(token);
            long expiryTime = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            // Expiring within 5 minutes
            return (expiryTime - now) < (5 * 60 * 1000);
        } catch (Exception e) {
            return true;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.warn("[JWT] Token expired for user: {}",
                e.getClaims().getSubject());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("[JWT] Invalid token: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
