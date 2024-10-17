package com.playground.service;

import com.playground.dto.MemberFormDto;
import com.playground.entity.Email;
import com.playground.entity.Member;
import com.playground.repository.EmailRepository;
import com.playground.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final EmailRepository emailRepository;

    @Override
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    @Override
    public void updateMember(String email, String password) {
        Member findMember = memberRepository.findByEmail(email);
        findMember.setPassword(password);
        memberRepository.save(findMember);
    }

    //추가
    @Override
    public void updateMemberAll(MemberFormDto dto) {
        Member findMember = memberRepository.findByEmail(dto.getEmail());
        findMember.setPassword(dto.getPassword());
        findMember.setPhone(dto.getPhone());
        findMember.setAddress(dto.getAddress());
        findMember.setAddressCode(dto.getAddressCode());
        findMember.setAddressDetail(dto.getAddressDetail());
        memberRepository.save(findMember);
    }

    //추가
    @Override
    public MemberFormDto getUser(String email){
        Member member=memberRepository.findByEmail(email);
        MemberFormDto dto=new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setAddressCode(member.getAddressCode());
        dto.setAddressDetail(member.getAddressDetail());
        dto.setName(member.getName());
        dto.setPhone(member.getPhone());
        return dto;
    }

    @Override
    public String validateEmail(String email) {
        String result="";
        Member findMember = memberRepository.findByEmail(email);
        if (findMember != null) {
            result="exist";
        } else{
            result="none";
        }
        return result;
    }

    @Override
    public void setCode(String email, String code) {
        Email exist = emailRepository.findByEmail(email);
        if (exist == null) {
            Email codeEmail = new Email();
            codeEmail.setEmail(email);
            codeEmail.setAuthCode(code);
            emailRepository.save(codeEmail);
        } else {
            exist.setAuthCode(code);
            emailRepository.save(exist);
        }
    }

    @Override
    public String compareCode(String email,String code) {
        String result="";
        Email getInfo=emailRepository.findByEmail(email);
        System.out.println(getInfo.getAuthCode());
        System.out.println(email);
        System.out.println(code);
        System.out.println("---------------------------");
        if(getInfo.getAuthCode().equals(code)){
            result="ok";
        } else{
            result="not";
        }
        return result;
    }

    @Override
    public void authDelete(String email) {
        emailRepository.deleteByEmail(email);
    }

    @Override
    public List<String> findEmail(String name, String phone) {
        Object[] emails=memberRepository.findEmail(name,phone);
        List<String> emailList = Arrays.stream(emails)
                .map(Object::toString) // Convert each Object to String
                .collect(Collectors.toList());
        return emailList;
    }

    // 비밀번호 변경 이메일 보내기 전 검증
    @Override
    public String validBeforeSendPwd(String email, String name, String phone) {
        String result="";

        Member member=memberRepository.findByEmail(email);
        if (member == null) {
            return result="noEmail";
        }
        if(member.getName().equals(name)&&member.getPhone().equals(phone)){
            return result="valid";
        }
        return result="notValid";
    }
    // 추가 끝
}
