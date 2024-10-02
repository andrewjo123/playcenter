package com.playground.service;

import com.playground.dto.MemberFormDto;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MemberRepository memberRepository;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("놀이마당<gitemail@naver.com>");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        System.out.println("Email 보냄");
    }

    public MemberFormDto getUser(String email){
        Member member=memberRepository.findByEmail(email);
        MemberFormDto dto=new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setName(member.getName());
        return dto;
    }
}
