package com.playground.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import com.playground.security.dto.PlayAuthMemberDTO;

import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Log4j2
@RequiredArgsConstructor
public class PlayUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("PlayUserDetailsService loadUserByUsername " + username);
        Optional<Member> result = memberRepository.findByEmailAndFromSocial(username, false);

        if(result.isEmpty()){
            throw new UsernameNotFoundException("Check User Email or from Social ");
        }

        Member clubMember = result.get();
        log.info("-----------------------------");
        log.info(clubMember);

        PlayAuthMemberDTO playAuthMember = new PlayAuthMemberDTO(
                clubMember.getEmail(),
                clubMember.getPassword(),
                clubMember.isFromSocial(),
                clubMember.getRoleSet().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_"+role.name()))
                        .collect(Collectors.toSet())
        );

        playAuthMember.setName(clubMember.getName());
        playAuthMember.setFromSocial(clubMember.isFromSocial());

        return playAuthMember;
    }
}