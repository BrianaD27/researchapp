package com.vsu.researchapp.application.service;

import com.vsu.researchapp.domain.model.LoginHistory;
import com.vsu.researchapp.domain.repository.LoginHistoryRepository;
import com.vsu.researchapp.domain.repository.UserAccountRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SecurityAuditService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserAccountRepository userAccountRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SecurityAuditService(
            LoginHistoryRepository loginHistoryRepository,
            UserAccountRepository userAccountRepository,
            RedisTemplate<String, Object> redisTemplate) {
        this.loginHistoryRepository = loginHistoryRepository;
        this.userAccountRepository = userAccountRepository;
        this.redisTemplate = redisTemplate;
    }

    // Full security dashboard
    public Map<String, Object> getSecurityDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        // Total users
        dashboard.put("totalUsers",
            userAccountRepository.count());

        // Locked accounts
        dashboard.put("lockedAccounts",
            userAccountRepository.countByAccountLocked(true));

        // Inactive accounts
        dashboard.put("inactiveAccounts",
            userAccountRepository.countByActive(false));

        // Recent failed logins last 24 hours
        dashboard.put("recentFailedLogins",
            getRecentFailedLogins());

        // Recent successful logins last 24 hours
        dashboard.put("recentSuccessfulLogins",
            getRecentSuccessfulLogins());

        // Blocked IPs in Redis
        dashboard.put("blockedIps", getBlockedIps());

        // Blacklisted tokens count
        dashboard.put("blacklistedTokens", getBlacklistedTokenCount());

        dashboard.put("generatedAt", LocalDateTime.now().toString());

        return dashboard;
    }

    public List<LoginHistory> getRecentFailedLogins() {
        return loginHistoryRepository
            .findByStatusOrderByLoginTimeDesc("FAILED");
    }

    public List<LoginHistory> getRecentSuccessfulLogins() {
        return loginHistoryRepository
            .findByStatusOrderByLoginTimeDesc("SUCCESS");
    }

    public List<LoginHistory> getInternationalLogins() {
        return loginHistoryRepository
            .findByStatusOrderByLoginTimeDesc("INTERNATIONAL_LOGIN");
    }

    private Set<String> getBlockedIps() {
        try {
            Set<String> keys = redisTemplate.keys("rate_limit:*");
            return keys != null ? keys : java.util.Collections.emptySet();
        } catch (Exception e) {
            return java.util.Collections.emptySet();
        }
    }

    private long getBlacklistedTokenCount() {
        try {
            Set<String> keys = redisTemplate.keys("blacklist:*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
