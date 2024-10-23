package com.playground.service;

import com.playground.dto.MemberFormDto;
import com.playground.entity.Member;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface MemberService extends UserDetailsService {
    Member saveMember(Member member);

    void updateMember(String email, String password);

    void updateMemberAll(MemberFormDto dto);

    //추가
    public MemberFormDto getUser(String email);
    public String validateEmail(String email);
    public void setCode(String email, String code);
    public String compareCode(String email, String code);
    public void authDelete(String email);
    public List<String> findEmail(String name, String phone);
    public String validBeforeSendPwd (String email, String name, String phone);
    public void registSocialMember(MemberFormDto dto);

    // 10.22 1600추가
    public void changeSocial(String email);

    // 10.23 1500추가
    public String valideResign(MemberFormDto dto);
    public void changeResign(String email);
}
