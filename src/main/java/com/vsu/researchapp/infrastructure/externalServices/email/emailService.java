package com.vsu.researchapp.infrastructure.externalServices.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class emailService {

    private static final Logger logger =
        LoggerFactory.getLogger(emailService.class);

    private final JavaMailSender mailSender;

    public emailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Send 2FA code
    public void send2FACode(String toEmail, String code) {
        sendEmail(
            toEmail,
            "VSU Research App - Verification Code",
            "Your verification code is: " + code + "\n\n" +
            "This code expires in 5 minutes.\n\n" +
            "If you did not request this code, please contact VSU IT immediately."
        );
    }

    // Send password reset link
    public void sendPasswordReset(String toEmail, String token) {
        sendEmail(
            toEmail,
            "VSU Research App - Password Reset",
            "You requested a password reset.\n\n" +
            "Use this token to reset your password: " + token + "\n\n" +
            "This token expires in 15 minutes.\n\n" +
            "If you did not request this, please ignore this email."
        );
    }

    // Send login alert for new IP
    public void sendLoginAlert(String toEmail, String ip,
            String userAgent) {
        sendEmail(
            toEmail,
            "VSU Research App - New Login Detected",
            "A login was detected from a new location.\n\n" +
            "IP Address: " + ip + "\n" +
            "Device: " + userAgent + "\n\n" +
            "If this was you, no action is needed.\n" +
            "If this was not you, reset your password immediately."
        );
    }

    // Send account locked notification
    public void sendAccountLocked(String toEmail) {
        sendEmail(
            toEmail,
            "VSU Research App - Account Locked",
            "Your account has been temporarily locked due to " +
            "multiple failed login attempts.\n\n" +
            "Your account will automatically unlock after 15 minutes.\n\n" +
            "If you did not attempt to log in, please contact VSU IT."
        );
    }

    // Send welcome email for new SSO users
    public void sendWelcome(String toEmail, String name) {
        sendEmail(
            toEmail,
            "Welcome to VSU Research App",
            "Hi " + name + ",\n\n" +
            "Your VSU Research App account has been created.\n\n" +
            "You can log in using your VSU Microsoft account at:\n" +
            "https://researchapp.vsu.edu\n\n" +
            "Virginia State University Research Department"
        );
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
