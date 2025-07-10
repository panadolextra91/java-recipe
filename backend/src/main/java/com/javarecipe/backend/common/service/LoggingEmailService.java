package com.javarecipe.backend.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Development implementation of EmailService that logs emails instead of sending them.
 * This is useful for local development without setting up an actual email server.
 */
@Service
@Profile({"dev", "default"})
public class LoggingEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEmailService.class);

    @Override
    public void sendSimpleEmail(String to, String subject, String text) {
        logger.info("Simulating email sending:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("Content: {}", text);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        logger.info("Simulating HTML email sending:");
        logger.info("To: {}", to);
        logger.info("Subject: {}", subject);
        logger.info("HTML Content: {}", htmlContent);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken, String username) {
        String subject = "Password Reset Request";
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        
        String content = String.format(
                "Hello %s,\n\n" +
                "You have requested to reset your password. Please click the link below to reset your password:\n\n" +
                "%s\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Regards,\nJava Recipe Team",
                username, resetLink);
        
        sendSimpleEmail(to, subject, content);
    }
} 