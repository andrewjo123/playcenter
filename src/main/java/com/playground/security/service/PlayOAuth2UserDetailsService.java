package com.playground.security.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.playground.entity.Member;
import com.playground.constant.Role;
import com.playground.repository.MemberRepository;
import com.playground.security.dto.PlayAuthMemberDTO;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.playground.constant.Role.USER;

@Log4j2
@Service
@RequiredArgsConstructor
public class PlayOAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        log.info("=====================================================");
        log.info("userRequest: " + userRequest);

        String clientName = userRequest.getClientRegistration().getClientName();

        log.info("clientName: " + clientName);
        log.info(userRequest.getAdditionalParameters());

        OAuth2User oAuth2User =  super.loadUser(userRequest);

        log.info("==============================");
        oAuth2User.getAttributes().forEach((k,v) -> {
            log.info(k +":" + v);
        });

        String email = null;
        // 구글외 카카오톡이거나 네이버이면 코드 추가한다. else if
        if(clientName.equals("Google")){
            email = oAuth2User.getAttribute("email");
        }

        log.info("EMAIL: " + email);

        Member member = saveSocialMember(email); //조금 뒤에 사용

        String role = "ROLE_" + member.getRole().name(); // 역할 이름에 ROLE_ 접두사 추가

        // 단일 권한 생성
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
        log.info("Granted Authority: " + authority);

        PlayAuthMemberDTO playAuthMember = new PlayAuthMemberDTO(
                member.getEmail(),
                member.getPassword(),
                true,   //fromSocial
                Set.of(authority),
//                member.getRoleSet().stream().map(
//                                role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
//                        .collect(Collectors.toList()),
                oAuth2User.getAttributes()
        );
        playAuthMember.setName(member.getName());


        return playAuthMember;
    }

    private Member saveSocialMember(String email){

        //기존에 동일한 이메일로 가입한 회원이 있는 경우에는 그대로 조회만
        Optional<Member> result = repository.findByEmailAndFromSocial(email, true);

        if(result.isPresent()){
            return result.get();
        }

        //없다면 회원 추가 패스워드는 1111 이름은 그냥 이메일 주소로
        Member clubMember = Member.builder().email(email)
                .name("홍길동")
                .password( passwordEncoder.encode("1111") )
                .fromSocial(true)
                .phone("010-1234-5678")
                .address("주소찾기를 이용해서 입력해주세요.")
                .addressCode("12345")
                .role(USER)
                .build();
        repository.save(clubMember);

        return clubMember;
    }

}