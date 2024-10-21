package com.playground.service;

import com.playground.dto.MemberFormDto;

public interface EmailService {
    public void sendEmail(String toEmail, String subject, String body);
    public MemberFormDto getUser(String email);
}
