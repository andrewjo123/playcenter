package com.playground.controller;

import com.playground.dto.MemberFormDto;
import com.playground.service.EmailService;
import com.playground.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Random;

@Controller
@RequestMapping("/test")
public class SendEmailController {

    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MemberService memberService;

    @GetMapping("/sendEmail")
    public void email(){}

    @PostMapping("/sendEmail")
    public String sendEmail(@RequestParam String toEmail,
                            @RequestParam String subject,
                            @RequestParam String body) {
        emailService.sendEmail(toEmail, subject, body);
        return "redirect:/";
    }

    @GetMapping("/findPw")
    public void findPw(){}

    @PostMapping("/findPw")
    @Transactional
    public String sendEmail(@RequestParam String toEmail) {
        String subject="[놀이마당]임시 비밀번호 전송";
        String newPw=randomMix(12);
        String body="비밀번호가 "+newPw+"로 변경되었습니다. \n\n안전한 사용을 위해 로그인 후 가급적 비밀번호를 변경해 주시길 바랍니다.";
        String encodedPw=passwordEncoder.encode(newPw);

        memberService.updateMember(toEmail, encodedPw);
        emailService.sendEmail(toEmail, subject, body);
        return "redirect:/";
    }

    public static String randomMix(int range) {
        StringBuilder sb = new StringBuilder();
        Random rd = new Random();

//        for(int i=0;i<range;i++){
//
//            if(rd.nextBoolean()){
//                sb.append(rd.nextInt(10));
//            }else {
//                sb.append((char)(rd.nextInt(26)+65));
//            }
//        }
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*?";

        for (int i = 0; i < range; i++) {
            int index = rd.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    @GetMapping("/modifyUser")
    public void modUser(Model model, Principal principal){
        String userid = principal.getName();
        MemberFormDto dto=emailService.getUser(userid);
        model.addAttribute("dto", dto);
    }

    @PostMapping("/modifyUser")
    public String modifyUser(MemberFormDto dto, Principal principal){
        String password= passwordEncoder.encode(dto.getPassword());
        memberService.updateMemberAll(principal.getName(), password, dto.getAddress());
        return "redirect:/members/login";
    }
}