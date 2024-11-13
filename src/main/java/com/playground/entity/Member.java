package com.playground.entity;

import com.playground.constant.Role;
import com.playground.dto.MemberFormDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="member")
@Getter @Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member extends BaseEntity {

    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String addressCode; // 우편번호

    @Column(nullable = false)
    private String address;

    private String addressDetail; // 상세주소

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean fromSocial; //소셜로그인 추가

//  @ElementCollection(fetch = FetchType.LAZY)
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private Set<Role> roleSet = new HashSet<>();

    public void addMemberRole(Role playMemberRole){

        roleSet.add(playMemberRole);
    }

    //추가
    @ColumnDefault("false")
    private boolean resign;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder){
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setPhone(memberFormDto.getPhone());
        member.setAddress(memberFormDto.getAddress());
        member.setAddressCode(memberFormDto.getAddressCode());
        member.setAddressDetail(memberFormDto.getAddressDetail());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.USER);
        member.setProfileImg("playGround");
        return member;
    }

    @ColumnDefault("0")
    private int totalPoint; // 현재 보유 포인트

    //프로필 이미지
    @ColumnDefault("'playGround'")
    private String profileImg;
}
