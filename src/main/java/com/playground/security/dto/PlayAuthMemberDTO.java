package com.playground.security.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

@Log4j2
@Getter
@Setter
@ToString
public class PlayAuthMemberDTO extends User implements OAuth2User {

    private String email;

    private String password;

    private String name;

    private boolean fromSocial;

    private Map<String, Object> attr;

    private String profileImg;

    public PlayAuthMemberDTO(String username, String password, boolean fromSocial,
                             Collection<? extends GrantedAuthority> authorities, Map<String, Object> attr, String profileImg) {
        this(username,password, fromSocial, authorities, profileImg);
        this.attr = attr;
    }

    public PlayAuthMemberDTO(String username, String password, boolean fromSocial, Collection<? extends GrantedAuthority> authorities, String profileImg) {
        super(username, password, authorities);
        this.email = username;
        this.password = password;
        this.fromSocial = fromSocial;
        this.profileImg = profileImg;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attr;
    }

}