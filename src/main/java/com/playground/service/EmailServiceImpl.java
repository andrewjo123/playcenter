package com.playground.service;

import com.playground.dto.MemberFormDto;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendEmail(String toEmail, String subject, String template, Context context) {
        try {
            String body = templateEngine.process(template, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("놀이마당<gitemail@naver.com>");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // true로 설정하여 HTML로 전송

            mailSender.send(message);
            System.out.println("Email 보냄: " + toEmail);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리
        }

        System.out.println("Email 일괄발송 완료");
    }

    @Override
    public MemberFormDto getUser(String email){
        Member member=memberRepository.findByEmail(email);
        MemberFormDto dto=new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setName(member.getName());
        return dto;
    }

    @Override
    public void sendEmailToMany(List<String> emailList, String subject, String template, Context context) {
        emailList.forEach(toEmail -> {
            try {
                String body = templateEngine.process(template, context);

                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setFrom("놀이마당<gitemail@naver.com>");
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(body, true); // true로 설정하여 HTML로 전송

                mailSender.send(message);
            } catch (Exception e) {
                e.printStackTrace(); // 예외 처리
            }
        });
        System.out.println("Email 일괄발송 완료");
    }

}
