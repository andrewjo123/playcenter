package com.playground.security.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import com.playground.security.dto.PlayAuthMemberDTO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlayUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Member result = memberRepository.findByEmail(username);


        // result가 존재하는지 확인하는 로그 추가
        if (result==null) {
            throw new UsernameNotFoundException("Check User Email or from Social");
        }

        Optional<Member> isResign=memberRepository.findByEmailAndResign(username, true);
        if(isResign.isPresent()){
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
            session.setAttribute("resign", "resign");
            throw new UsernameNotFoundException("Check User Email or from Social");
        }

        String role = "ROLE_" + result.getRole().name(); // 역할 이름에 ROLE_ 접두사 추가

        // 단일 권한 생성
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        PlayAuthMemberDTO playAuthMember = new PlayAuthMemberDTO(
                result.getEmail(),
                result.getPassword(),
                false,
                Set.of(authority),
//                playMember.getRoleSet().stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role.name()))
//                        .collect(Collectors.toSet())
                result.getProfileImg()
        );
        playAuthMember.setName(result.getName());

        return playAuthMember;
    }
}