package com.minden.infrastructure.email;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String username) throws Exception;
    boolean verifyEmailExists(String email);
}
