package com.playground.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import com.playground.security.dto.PlayAuthMemberDTO;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
public class PlayUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("PlayUserDetailsService loadUserByUsername " + username);
        Member result = memberRepository.findByEmail(username);


        // result가 존재하는지 확인하는 로그 추가
        if (result==null) {
            log.warn("No member found with email: " + username + " and fromSocial: false");
            throw new UsernameNotFoundException("Check User Email or from Social");
        }

        Optional<Member> isResign=memberRepository.findByEmailAndResign(username, true);
        if(isResign.isPresent()){
            throw new UsernameNotFoundException("Check User Email or from Social");
        }

        String role = "ROLE_" + result.getRole().name(); // 역할 이름에 ROLE_ 접두사 추가

        // 단일 권한 생성
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        log.info("Granted Authority: " + authority);

        PlayAuthMemberDTO playAuthMember = new PlayAuthMemberDTO(
                result.getEmail(),
                result.getPassword(),
                false,
                Set.of(authority)
//                playMember.getRoleSet().stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role.name()))
//                        .collect(Collectors.toSet())
        );
        playAuthMember.setName(result.getName());

        return playAuthMember;
    }
}