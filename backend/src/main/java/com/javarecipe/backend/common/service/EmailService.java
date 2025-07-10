package com.javarecipe.backend.common.service;

public interface EmailService {

    /**
     * Send a simple text email
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param text email body
     */
    void sendSimpleEmail(String to, String subject, String text);
    
    /**
     * Send an HTML formatted email
     * 
     * @param to recipient email address
     * @param subject email subject
     * @param htmlContent email body in HTML format
     */
    void sendHtmlEmail(String to, String subject, String htmlContent);
    
    /**
     * Send a password reset email with a reset link
     * 
     * @param to recipient email address
     * @param resetToken the password reset token
     * @param username the user's username
     */
    void sendPasswordResetEmail(String to, String resetToken, String username);
} 