package com.playground.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class CustomOAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        String error = (String) request.getSession().getAttribute("error");
        request.getSession().removeAttribute("error");

        String redirectUrl="";
        if ("add".equals(error)) {
            request.getSession().removeAttribute("site");
            redirectUrl = "/members/modify2";
        } else if ("merge".equals(error)) {
            redirectUrl = "/members/confirm";
        } else {
            request.getSession().removeAttribute("site");
            request.getSession().removeAttribute("email");
            redirectUrl = "/members/login/error";
        }
        response.sendRedirect(redirectUrl);
    }
}
