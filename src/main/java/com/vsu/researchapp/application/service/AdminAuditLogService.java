package com.vsu.researchapp.application.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vsu.researchapp.domain.model.AdminAuditLog;
import com.vsu.researchapp.domain.repository.AdminAuditLogRepository;

@Service
public class AdminAuditLogService {

    private final AdminAuditLogRepository repo;

    public AdminAuditLogService(AdminAuditLogRepository repo) {
        this.repo = repo;
    }

    //  Keep your existing 3-arg method
    public void log(String actor, String action, String details) {
        AdminAuditLog entry = new AdminAuditLog();
        entry.setActor(safe(actor));
        entry.setAction(safe(action));
        entry.setDetails(safe(details));
        repo.save(entry);
    }

    //  NEW: allow log("ACTION")
    public void log(String action) {
        log(currentUsernameOrSystem(), action, "");
    }

    //  NEW: allow log("ACTION", "DETAILS")
    public void log(String action, String details) {
        log(currentUsernameOrSystem(), action, details);
    }

    private String currentUsernameOrSystem() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return "SYSTEM";
            return auth.getName();
        } catch (Exception e) {
            return "SYSTEM";
        }
    }

    private String safe(String s) {
        return (s == null) ? "" : s;
    }
}
