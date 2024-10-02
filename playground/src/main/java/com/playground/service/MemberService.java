package com.playground.service;

import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public Member saveMember(Member member){
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    private void validateDuplicateMember(Member member){
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if(findMember != null){
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member member = memberRepository.findByEmail(email);

        if(member == null){
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    // 비밀번호 이메일로 수정 (추가)
    public void updateMember(String email, String password){
        Member findMember = memberRepository.findByEmail(email);
        findMember.setPassword(password);
        memberRepository.save(findMember);
    }

    // 유저 정보 수정 (추가)
    public void updateMemberAll(String email, String password, String address){
        Member findMember = memberRepository.findByEmail(email);
        findMember.setPassword(password);
        findMember.setAddress(address);
        memberRepository.save(findMember);
    }

}