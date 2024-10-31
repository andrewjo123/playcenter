package com.playground.config;

import com.playground.security.service.PlayUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.playground.security.handler.PlayLoginSuccessHandler;
import com.playground.security.service.PlayOAuth2UserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PlayUserDetailsService playUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(PlayUserDetailsService playUserDetailsService, PasswordEncoder passwordEncoder) {
        this.playUserDetailsService = playUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private PlayOAuth2UserDetailsService playOAuth2UserDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.formLogin(formLoginCustomizer -> formLoginCustomizer
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureHandler(new CustomAuthenticationFailureHandler())
        ).logout( logoutCustomizer -> logoutCustomizer
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/members/login")
        ).oauth2Login(oauth2->oauth2
                .loginPage("/members/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(playOAuth2UserDetailsService))
                .successHandler(playLoginSuccessHandler())
                .failureHandler(new CustomOAuth2AuthenticationFailureHandler())
        ).exceptionHandling(exceptionHandlingCustomizer -> exceptionHandlingCustomizer
                .accessDeniedPage("/error") // 접근 권한이 없을 때 에러 페이지 설정
                .defaultAuthenticationEntryPointFor(
                        (request, response, authException) -> response.sendRedirect("/error"),
                        new AntPathRequestMatcher("/**") // 모든 URL에 대해 예외 발생 시 리디렉션
                )
        );

        return http.build();
    }

    @Bean
    public PlayLoginSuccessHandler playLoginSuccessHandler() {
        return new PlayLoginSuccessHandler(passwordEncoder);
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(playUserDetailsService).passwordEncoder(passwordEncoder);
    }


//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
//        authBuilder.userDetailsService(playUserDetailsService).passwordEncoder(passwordEncoder);
//        return authBuilder.build();
//    }

}