package com.playground.security.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.playground.entity.Member;
import com.playground.repository.MemberRepository;
import com.playground.security.dto.PlayAuthMemberDTO;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlayOAuth2UserDetailsService extends DefaultOAuth2UserService {

    private final MemberRepository repository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest){

        String clientName = userRequest.getClientRegistration().getClientName();

        OAuth2User oAuth2User =  super.loadUser(userRequest);

        String email = null;
        // 구글외 카카오톡이거나 네이버이면 코드 추가한다. else if
        if(clientName.equals("Google")){
            email = oAuth2User.getAttribute("email");
        } else if(clientName.equals("Naver")){
            Map<String, Object> response= oAuth2User.getAttribute("response");
            if (response != null) {
                email=(String) response.get("email");
            }
        } else if(clientName.equals("Kakao")){
            Map<String, Object> response= oAuth2User.getAttribute("kakao_account");
            if (response != null) {
                email=(String) response.get("email");
            }
        }

        Optional<Member> isResign=repository.findByEmailAndResign(email, true);
        if(isResign.isPresent()){
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
            session.setAttribute("resign", "resign");
            throw new UsernameNotFoundException("Check User Email or from Social");
        }

        Optional<Member> result = repository.findByEmailAndFromSocial(email, true);
        Optional<Member> result2 = repository.findByEmailAndFromSocial(email, false);

        Member member;
        if(result.isPresent()){
            member= result.get();
        } else if (result2.isPresent()) {
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
            session.setAttribute("email", email);
            session.setAttribute("error","merge");
            //다른 소셜로그인 쓸거면 추가
            if(clientName.equals("Google")){
                session.setAttribute("site","google");
            }else if(clientName.equals("Naver")){
                session.setAttribute("site","naver");
            }else if(clientName.equals("Kakao")){
                session.setAttribute("site","kakao");
            }
            throw new OAuth2AuthenticationException("계정병합을 하시겠습니까.");
        } else{
            // 세션에 이메일 저장
            HttpSession session = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getSession();
            session.setAttribute("email", email);
            session.setAttribute("error","add");
            throw new OAuth2AuthenticationException("추가 정보가 필요합니다.");
        }

        String role = "ROLE_" + member.getRole().name(); // 역할 이름에 ROLE_ 접두사 추가

        // 단일 권한 생성
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        PlayAuthMemberDTO playAuthMember = new PlayAuthMemberDTO(
                member.getEmail(),
                member.getPassword(),
                true,   //fromSocial
                Set.of(authority),
//                member.getRoleSet().stream().map(
//                                role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
//                        .collect(Collectors.toList()),
                oAuth2User.getAttributes(),
                member.getProfileImg()
        );
        playAuthMember.setName(member.getName());


        return playAuthMember;
    }

}