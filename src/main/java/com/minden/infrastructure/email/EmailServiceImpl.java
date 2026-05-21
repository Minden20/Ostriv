package com.minden.infrastructure.email;

import java.util.Properties;
import jakarta.mail.*;
import jakarta.mail.internet.*;

public class EmailServiceImpl implements EmailService {

    private final String username;
    private final String password;
    private final String host;
    private final String port;

    public EmailServiceImpl() {
        // Завантажуємо конфігурацію з середовища (Environment) або використовуємо базові налаштування
        this.username = System.getenv("SMTP_USERNAME") != null ? System.getenv("SMTP_USERNAME") : "olimps90@gmail.com";
        this.password = System.getenv("SMTP_PASSWORD") != null ? System.getenv("SMTP_PASSWORD") : "jjkrjzzrdsyliwmk";
        this.host = System.getenv("SMTP_HOST") != null ? System.getenv("SMTP_HOST") : "smtp.gmail.com";
        this.port = System.getenv("SMTP_PORT") != null ? System.getenv("SMTP_PORT") : "587";
    }

    @Override
    public void sendVerificationEmail(String toEmail, String username) throws Exception {
        System.out.println("[EmailService] Спроба відправки реального листа на: " + toEmail);

        if (this.password == null || this.password.trim().isEmpty()) {
            System.out.println("[EmailService] Пароль SMTP не налаштовано. Лист симулюється в логах для розробки.");
            System.out.println("[EmailService] ТЕКСТ ЛИСТА: Вітаємо " + username + "! Ваш акаунт на " + toEmail + " підтверджено.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EmailServiceImpl.this.username, EmailServiceImpl.this.password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(this.username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("RPG Admin System - Підтвердження реєстрації");
            String htmlContent = "<h3>Вітаємо, " + username + "!</h3>"
                    + "<p>Ваш акаунт було успішно створено та перевірено в RPG Admin Panel.</p>";
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("[EmailService] Лист успішно надіслано на: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("[EmailService] Помилка відправки листа через SMTP: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean verifyEmailExists(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }
        return true;
    }
}
