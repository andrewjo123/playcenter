package com.playground.repository;

import com.playground.constant.Role;
import com.playground.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    public void setup() {
        // 더미 회원 데이터 생성
        Member member1 = Member.builder()
                .name("조훈")
                .email("andrewjo123@hanmail.net")
                .password(passwordEncoder.encode("password1"))
                .phone("010-0223-4256")
                .address("서울 강남구 논현로121길 12 (논현동)")
                .addressCode("06116")
                .addressDetail("102-201")
                .role(Role.ADMIN)
                .fromSocial(true)
                .resign(false)
                .totalPoint(1000)
                .build();

        Member member2 = Member.builder()
                .name("조자룡")
                .email("andrewjoitkr@gmail.com")
                .password(passwordEncoder.encode("password2"))
                .phone("010-1234-5678")
                .address("서울 강북구 4.19로11길 6 (수유동)")
                .addressCode("01014")
                .role(Role.USER)
                .fromSocial(true)
                .resign(false)
                .totalPoint(500)
                .build();

        memberRepository.save(member1);
        memberRepository.save(member2);
    }

    @Test
    public void testFindByEmail() {
        // 이메일로 회원 조회 테스트
        Member member = memberRepository.findByEmail("andrewjo123@hanmail.net");
        assertThat(member).isNotNull();
        assertThat(member.getName()).isEqualTo("조훈");
    }

    @Test
    public void testFindEmailByNameAndPhone() {
        // 이름과 전화번호로 이메일 조회 테스트
        Object[] result = memberRepository.findEmail("조자룡", "010-1234-5678");
        assertThat(result).isNotNull();
        assertThat(result[0]).isEqualTo("andrewjoitkr@gmail.com");
    }

    @Test
    public void testFindByEmailAndFromSocial() {
        // 이메일과 소셜 로그인 여부로 회원 조회 테스트
        Optional<Member> memberOpt = memberRepository.findByEmailAndFromSocial("andrewjo123@hanmail.net", true);
        assertThat(memberOpt).isPresent();
        assertThat(memberOpt.get().getName()).isEqualTo("조훈");
    }

    @Test
    public void testFindByEmailAndResign() {
        // 이메일과 탈퇴 여부로 회원 조회 테스트
        Optional<Member> memberOpt = memberRepository.findByEmailAndResign("andrewjoitkr@gmail.com", false);
        assertThat(memberOpt).isPresent();
        assertThat(memberOpt.get().getName()).isEqualTo("조자룡");
    }
}
