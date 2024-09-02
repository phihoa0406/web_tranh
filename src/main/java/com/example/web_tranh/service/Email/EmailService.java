package com.example.web_tranh.service.Email;

public interface EmailService {
    public void sendMessage(String from, String to, String subject, String message);
}
