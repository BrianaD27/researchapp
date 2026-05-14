package com.vsu.researchapp.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ThreatEventPublisher {

    private static final Logger logger =
        LoggerFactory.getLogger(ThreatEventPublisher.class);

    private final SimpMessagingTemplate messagingTemplate;

    public ThreatEventPublisher(
            SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishThreatEvent(String eventType,
            String username, String ip, String details) {
        try {
            Map<String, Object> event = Map.of(
                "type", eventType,
                "username", username != null ? username : "anonymous",
                "ip", ip != null ? ip : "unknown",
                "details", details != null ? details : "",
                "timestamp", LocalDateTime.now().toString(),
                "severity", getSeverity(eventType)
            );

            messagingTemplate.convertAndSend(
                "/topic/threats", (Object) event);

            logger.info("[THREAT EVENT] {} - {} - {}",
                eventType, username, ip);
        } catch (Exception e) {
            logger.error("[THREAT] Could not publish event: {}",
                e.getMessage());
        }
    }

    private String getSeverity(String eventType) {
        return switch (eventType) {
            case "BRUTE_FORCE", "HONEYPOT_TRIGGERED",
                "SQL_INJECTION" -> "CRITICAL";
            case "ACCOUNT_LOCKED", "PRIVILEGE_ESCALATION",
                "INTERNATIONAL_LOGIN" -> "HIGH";
            case "FAILED_LOGIN", "RATE_LIMIT_EXCEEDED" -> "MEDIUM";
            default -> "LOW";
        };
    }
}
