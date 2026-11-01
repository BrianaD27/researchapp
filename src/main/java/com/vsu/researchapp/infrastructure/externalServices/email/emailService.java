package com.vsu.researchapp.infrastructure.externalServices.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class emailService {

    private static final Logger logger =
        LoggerFactory.getLogger(emailService.class);

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public emailService(JavaMailSender mailSender,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    }

    public void send2FACode(String toEmail, String code) {
        sendEmail(toEmail,
            "VSU Research App - Verification Code",
            "Your verification code is: " + code + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "If you did not request this, contact VSU IT immediately."
        );
    }

    public void sendPasswordReset(String toEmail, String token) {
        sendEmail(toEmail,
            "VSU Research App - Password Reset",
            "Use this link to reset your password:\n\n" +
            frontendUrl + "/reset-password?token=" + token + "\n\n" +
            "The link expires in 15 minutes and works once.\n\n" +
            "If you did not request this, you can ignore this email."
        );
    }

    public void sendEmailVerification(String toEmail, String username, String token) {
        sendEmail(toEmail,
            "VSU Research App - Activate your account",
            "Hi " + username + ",\n\n" +
            "Click this link to confirm this is your VSU email and activate your account:\n\n" +
            frontendUrl + "/verify-email?token=" + token + "\n\n" +
            "The link expires in 24 hours and works once.\n\n" +
            "If you did not sign up, you can ignore this email and the account will stay inactive."
        );
    }

    // Mirrors an in-app notification. The link is a relative app path.
    public void sendNotification(String toEmail, String subject, String message, String link) {
        String body = message + "\n\n"
            + (link != null && link.startsWith("/") ? "Open it here: " + frontendUrl + link + "\n\n" : "")
            + "You are receiving this because you have an account on the VSU Research App.";
        sendEmail(toEmail, "VSU Research App - " + subject, body);
    }

    public void sendLoginAlert(String toEmail, String ip,
            String userAgent) {
        sendEmail(toEmail,
            "VSU Research App - New Login Detected",
            "A login was detected from a new location.\n\n" +
            "IP Address: " + ip + "\n" +
            "Device: " + userAgent + "\n\n" +
            "If this was not you, reset your password immediately."
        );
    }

    public void sendAccountLocked(String toEmail) {
        sendEmail(toEmail,
            "VSU Research App - Account Locked",
            "Your account has been temporarily locked due to " +
            "multiple failed login attempts.\n\n" +
            "It will automatically unlock after 15 minutes.\n\n" +
            "If you did not attempt to log in, contact VSU IT."
        );
    }

    public void sendWelcome(String toEmail, String name) {
        sendEmail(toEmail,
            "Welcome to VSU Research App",
            "Hi " + name + ",\n\n" +
            "Your VSU Research App account has been created.\n\n" +
            "Log in at: https://researchapp.vsu.edu\n\n" +
            "Virginia State University Research Department"
        );
    }

    public void sendSecurityReport(String toEmail, String report) {
        sendEmail(toEmail,
            "VSU Research App - Weekly Security Report", report);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("researchapp@vsu.edu");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("[EMAIL] Sent '{}' to {}", subject, to);
        } catch (Exception e) {
            logger.error("[EMAIL] Failed to send '{}' to {}: {}",
                subject, to, e.getMessage());
        }
    }
}