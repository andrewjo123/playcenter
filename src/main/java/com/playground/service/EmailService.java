package com.playground.service;

import com.playground.dto.MemberFormDto;
import org.thymeleaf.context.Context;

import java.util.List;

public interface EmailService {
    public void sendEmail(String toEmail, String subject, String template, Context context);
    public MemberFormDto getUser(String email);
    public void sendEmailToMany(List<String> emails, String subject, String template, Context context);
}
