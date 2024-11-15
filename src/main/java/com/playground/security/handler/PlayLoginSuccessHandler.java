package com.playground.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.playground.security.dto.PlayAuthMemberDTO;

import jakarta.servlet.*;
import java.io.IOException;

public class PlayLoginSuccessHandler implements AuthenticationSuccessHandler {

    private RedirectStrategy redirectStratgy = new DefaultRedirectStrategy();
    private PasswordEncoder passwordEncoder;
    //컨트롤러로 보내기 위한 코드
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    public PlayLoginSuccessHandler(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {


        PlayAuthMemberDTO authMember = (PlayAuthMemberDTO)authentication.getPrincipal();
        boolean fromSocial = authMember.isFromSocial(); //소셜 로그인 여부.
        boolean passwordResult = passwordEncoder.matches("1111", authMember.getPassword());
        if (fromSocial && passwordResult) {
            // RedirectStrategy를 사용하여 리다이렉트 처리
            redirectStrategy.sendRedirect(request, response, "/members/modify2");
        }else {
            // 소셜 로그인 사용자가 이미 비밀번호를 수정했거나 일반 사용자인 경우 리스트 페이지로 리다이렉트
            redirectStrategy.sendRedirect(request, response, "/");
        }
    }

}