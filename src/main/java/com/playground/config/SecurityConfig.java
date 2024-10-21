package com.playground.config;

import com.playground.security.service.PlayUserDetailsService;
import com.playground.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.playground.security.handler.PlayLoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final PlayUserDetailsService playUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SecurityConfig(PlayUserDetailsService playUserDetailsService, PasswordEncoder passwordEncoder) {
        this.playUserDetailsService = playUserDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    com.playground.service.MemberService memberService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeHttpRequestsCustomizer -> authorizeHttpRequestsCustomizer
//                        .requestMatchers("/css/**", "/js/**", "/img/**","/**").permitAll()
//                        .requestMatchers("/", "/members/**", "/item/**", "/images/**", "/test/**","/reviews/**").permitAll()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
        ).formLogin(formLoginCustomizer -> formLoginCustomizer
                .loginPage("/members/login")
                .defaultSuccessUrl("/")
                .usernameParameter("email")
                .failureUrl("/members/login/error")
                .failureHandler(new CustomAuthenticationFailureHandler())
        ).logout( logoutCustomizer -> logoutCustomizer
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout"))
                .logoutSuccessUrl("/members/login")
        ).oauth2Login(oauth2->oauth2
                .loginPage("/members/login")
                .successHandler(playLoginSuccessHandler())
        );

        return http.build();
    }

    @Bean
    public PlayLoginSuccessHandler playLoginSuccessHandler() {
        return new PlayLoginSuccessHandler(passwordEncoder);
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(playUserDetailsService).passwordEncoder(passwordEncoder);
        return authBuilder.build();
    }

}