package com.vsu.researchapp.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.model.MessagingPrefs;
import com.vsu.researchapp.domain.repositoryinterfaces.MessagingPrefsRepository;

// "Active now" dot. The app pings every minute while a tab is open; anyone seen
// in the last 2 minutes who hasn't turned the setting off shows as active.
// Kept in memory on purpose: a restart just clears the dots.
@Service
public class PresenceService {

    private static final Duration ACTIVE_WINDOW = Duration.ofMinutes(2);

    private final Map<Long, Instant> lastSeen = new ConcurrentHashMap<>();
    private final MessagingPrefsRepository prefs;

    public PresenceService(MessagingPrefsRepository prefs) {
        this.prefs = prefs;
    }

    public void heartbeat(Long userAccountId) {
        if (userAccountId != null) lastSeen.put(userAccountId, Instant.now());
    }

    public boolean isActive(Long userAccountId) {
        if (userAccountId == null) return false;
        Instant seen = lastSeen.get(userAccountId);
        if (seen == null || seen.isBefore(Instant.now().minus(ACTIVE_WINDOW))) return false;
        return showActive(userAccountId);
    }

    public boolean showActive(Long userAccountId) {
        return prefs.findById(userAccountId).map(MessagingPrefs::isShowActive).orElse(true);
    }

    public void setShowActive(Long userAccountId, boolean show) {
        MessagingPrefs p = prefs.findById(userAccountId).orElseGet(() -> {
            MessagingPrefs created = new MessagingPrefs();
            created.setUserAccountId(userAccountId);
            return created;
        });
        p.setShowActive(show);
        prefs.save(p);
    }
}
