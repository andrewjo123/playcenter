package com.playground.service;

import com.playground.dto.*;
import com.playground.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface MemberService extends UserDetailsService {
    Member saveMember(Member member);

    void updateMember(String email, String password);

    void updateMemberAll(MemberFormDto dto);

    //추가
    public MemberFormDto getUser(String email);
    public String validateEmail(String email);
    public void setCode(String email, String code);
    public String compareCode(String email, String code);
    public void authDelete(String email);
    public List<String> findEmail(String name, String phone);
    public String validBeforeSendPwd (String email, String name, String phone);
    public void registSocialMember(MemberFormDto dto);

    // 10.22 1600추가
    public void changeSocial(String email);

    // 10.23 1500추가
    public String valideResign(MemberFormDto dto);
    public void changeResign(String email);

    //조민 - 회원목록 불러오기
    Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable);

    //조민 - 활성상태변경
    void toggleStatus(Long memberId, boolean resign);

    //1031 1650 - 포인트 내역
    List<PointHistDto> getPointHistory(String email, int date);

    //1101 1720 - 비밀번호 찾기
    String createPasswordResetToken(String email);
    String validPwToken(String token);
    String findEmailFromToken(String token);

    //vip 테스트
    ChallengeDto getChallengeInfo(String email);
    String validAndAcceptChallenge(String challenge, String email);

    String changeProfileImg(String profileImg, String email);
}
