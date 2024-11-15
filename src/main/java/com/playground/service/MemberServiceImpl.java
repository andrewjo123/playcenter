package com.playground.service;

import com.playground.constant.Role;
import com.playground.dto.ChallengeDto;
import com.playground.dto.MemberFormDto;
import com.playground.dto.MemberSearchDto;
import com.playground.dto.PointHistDto;
import com.playground.entity.*;
import com.playground.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final EmailRepository emailRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    private final DibsRepository dibsRepository;
    private final MemberPointRepository pointRepository;
    private final PasswordResetRepository passwordRepository;
    private final MemberChallengeRepsoitory challengeRepository;

    @Override
    public Member saveMember(Member member) {
        validateDuplicateMember(member);

        Member member1=memberRepository.save(member);
        Cart cart = Cart.createCart(member1);
        cartRepository.save(cart);
        Dibs dibs= Dibs.createDibs(member1);
        dibsRepository.save(dibs);
        MemberChallenge challenge=new MemberChallenge();
        challenge.setMember(member1);
        challengeRepository.save(challenge);
        return member1;
    }

    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail());
        if (findMember != null) {
            throw new IllegalStateException("이미 가입된 회원입니다.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException(email);
        }

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }

    @Override
    public void updateMember(String email, String password) {
        Member findMember = memberRepository.findByEmail(email);
        findMember.setPassword(password);
        memberRepository.save(findMember);
    }

    //추가
    @Override
    public void updateMemberAll(MemberFormDto dto) {
        Member findMember = memberRepository.findByEmail(dto.getEmail());
        findMember.setPassword(dto.getPassword());
        findMember.setPhone(dto.getPhone());
        findMember.setAddress(dto.getAddress());
        findMember.setAddressCode(dto.getAddressCode());
        findMember.setAddressDetail(dto.getAddressDetail());
        memberRepository.save(findMember);
    }

    //추가
    @Override
    public MemberFormDto getUser(String email) {
        Member member = memberRepository.findByEmail(email);
        MemberFormDto dto = new MemberFormDto();
        dto.setEmail(member.getEmail());
        dto.setAddress(member.getAddress());
        dto.setAddressCode(member.getAddressCode());
        dto.setAddressDetail(member.getAddressDetail());
        dto.setName(member.getName());
        dto.setPhone(member.getPhone());
        dto.setTotalPoint(member.getTotalPoint());
        dto.setProfileImg(member.getProfileImg());
        return dto;
    }

    @Override
    public String validateEmail(String email) {
        String result = "";
        Member findMember = memberRepository.findByEmail(email);
        if (findMember != null) {
            result = "exist";
        } else {
            result = "none";
        }
        return result;
    }

    @Override
    public void setCode(String email, String code) {
        Email exist = emailRepository.findByEmail(email);
        if (exist == null) {
            Email codeEmail = new Email();
            codeEmail.setEmail(email);
            codeEmail.setAuthCode(code);
            emailRepository.save(codeEmail);
        } else {
            exist.setAuthCode(code);
            emailRepository.save(exist);
        }
    }

    @Override
    public String compareCode(String email, String code) {
        String result = "";
        Email getInfo = emailRepository.findByEmail(email);
        if (getInfo.getAuthCode().equals(code)) {
            result = "ok";
        } else {
            result = "not";
        }
        return result;
    }

    @Override
    public void authDelete(String email) {
        emailRepository.deleteByEmail(email);
    }

    @Override
    public List<String> findEmail(String name, String phone) {
        Object[] emails = memberRepository.findEmail(name, phone);
        List<String> emailList = Arrays.stream(emails)
                .map(Object::toString) // Convert each Object to String
                .collect(Collectors.toList());
        return emailList;
    }

    // 비밀번호 변경 이메일 보내기 전 검증
    @Override
    public String validBeforeSendPwd(String email, String name, String phone) {
        String result = "";

        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            return result = "noEmail";
        }
        if (member.getName().equals(name) && member.getPhone().equals(phone)) {
            if(passwordRepository.findByMember_Id(member.getId()).isPresent()) {
                if (!passwordRepository.findByMember_Id(member.getId()).get().isExpired()){
                    Duration duration=Duration.between(LocalDateTime.now(),passwordRepository.findByMember_Id(member.getId()).get().getExpirationTime());
                    return duration.toMinutes()+"분 "+(duration.getSeconds()%60)+"초";
                }
            }
            return result = "valid";
        }
        return result = "notValid";
    }

    @Override
    public void registSocialMember(MemberFormDto dto) {
        Member sMember = new Member();
        sMember.setName(dto.getName());
        sMember.setEmail(dto.getEmail());
        sMember.setPassword(dto.getPassword());
        sMember.setPhone(dto.getPhone());
        sMember.setAddress(dto.getAddress());
        sMember.setAddressCode(dto.getAddressCode());
        sMember.setAddressDetail(dto.getAddressDetail());
        sMember.setFromSocial(true);
        sMember.setRole(Role.USER);
        sMember.setProfileImg("playGround");
        memberRepository.save(sMember);

        Cart cart = Cart.createCart(sMember);
        cartRepository.save(cart);
        Dibs dibs=Dibs.createDibs(sMember);
        dibsRepository.save(dibs);
        MemberChallenge challenge=new MemberChallenge();
        challenge.setMember(sMember);
        challengeRepository.save(challenge);
    }

    // 1022 1600 추가
    @Override
    public void changeSocial(String email) {
        Member nonSocial = memberRepository.findByEmail(email);
        nonSocial.setFromSocial(true);
        memberRepository.save(nonSocial);
    }

    // 1023 1500 추가
    @Override
    public String valideResign(MemberFormDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail());
        String result = "";
        if (member != null && passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            result = "success";
        } else {
            result = "notFound";
        }
        return result;
    }

    @Override
    public void changeResign(String email) {
        Member member = memberRepository.findByEmail(email);
        member.setResign(!member.isResign());
        memberRepository.save(member);
    }

    //조민추가
    @Transactional(readOnly = true)
    @Override
    public Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable) {
        return memberRepository.getAdminMemberPage(memberSearchDto, pageable);
    }

    @Transactional
    public void toggleStatus(Long memberId, boolean resign) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버"));
        member.setResign(resign);  // 상태 변경
        memberRepository.save(member);
    }

    @Override
    public List<PointHistDto> getPointHistory(String email, int date) {
        List<Object[]> list=pointRepository.getPointHistory(email, date);

        List<PointHistDto> dtoList = new ArrayList<>();
        for (Object[] objArray : list) {
            PointHistDto dto = new PointHistDto();
            Timestamp timestamp = (Timestamp) objArray[0];
            dto.setUseDate(timestamp.toLocalDateTime());
            dto.setPayPoint((Integer) objArray[1]);
            if(objArray[2]!=null){
                dto.setOrderStatus((String) objArray[2]);
            }else{
                dto.setOrderStatus("challenge");
            }
            dtoList.add(dto);
        }

        return dtoList;
    }
    //조민 끝

    public String createPasswordResetToken(String email) {
        String token = UUID.randomUUID().toString();
        Member member=memberRepository.findByEmail(email);
        Optional<PasswordReset> passwordReset=passwordRepository.findByMember_Id(member.getId());
        if(passwordReset.isEmpty()){
            PasswordReset newPasswordReset = new PasswordReset(token, member);
            passwordRepository.save(newPasswordReset);
        } else{
            if(passwordReset.get().isExpired()){
                passwordReset.get().setToken(token);
                passwordReset.get().setExpirationTime(LocalDateTime.now().plusMinutes(10));
                passwordRepository.save(passwordReset.get());
            }
        }
        return token;
    }

    public String validPwToken(String token){
        Optional<PasswordReset> passwordReset=passwordRepository.findByToken(token);
        String result="";
        if(passwordReset.isPresent()){
            if(passwordReset.get().isExpired()){
                result="notValid";
            }else{
                result="valid";
            }
        }else{
            result="notValid";
        }
        return result;
    }
    public String findEmailFromToken(String token){
        Optional<PasswordReset> passwordReset=passwordRepository.findByToken(token);
        return passwordReset.get().getMember().getEmail();
    }

    @Override
    public ChallengeDto getChallengeInfo(String email) {
        MemberChallenge memberChallenge=challengeRepository.findByMemeberEmail(email);
        return ChallengeDto.of(memberChallenge);
    }

    @Override
    public String validAndAcceptChallenge(String challenge, String email) {
        String result="notValid";
        int getPoint=0;
        MemberChallenge allChallenge=challengeRepository.findByMemeberEmail(email);
        if(challenge.startsWith("pay")){
            String num=challenge.substring(3);
            int money=allChallenge.getUsedTotalMoney();
            int valid=allChallenge.getAchievementForMoney();
            if(num.equals("10")){
                if(money<100000||valid!=0){return result;}
                else{
                    allChallenge.setAchievementForMoney(1); result="10";
                    getPoint=1000;
                }
            } else if (num.equals("40")) {
                if(money<400000||valid!=1){return result;}
                else{
                    allChallenge.setAchievementForMoney(2); result="40";
                    getPoint=5000;
                }
            } else{
                if(money<1000000||valid!=2){return result;}
                else{
                    allChallenge.setAchievementForMoney(3); result="100";
                    getPoint=15000;
                }
            }
        } else{
            if(challenge.equals("steam")){
                if(allChallenge.getCountForSteam()<10||allChallenge.getAchievementForSteam()!=0){return result;}
                else {
                    allChallenge.setAchievementForSteam(1); result="steam";
                    getPoint=2000;
                }
            }
            if(challenge.equals("nintendo")){
                if(allChallenge.getCountForNintendo()<10||allChallenge.getAchievementForNintendo()!=0){return result;}
                else {
                    allChallenge.setAchievementForNintendo(1); result="nintendo";
                    getPoint=2000;
                }
            }
            if(challenge.equals("ps")){
                if(allChallenge.getCountForPs()<10||allChallenge.getAchievementForPs()!=0){return result;}
                else {
                    allChallenge.setAchievementForPs(1); result="ps";
                    getPoint=2000;
                }
            }
        }
        if(getPoint!=0){
            MemberPoint point=new MemberPoint();
            point.setPayPoint(getPoint);
            point.setEmail(email);
            pointRepository.save(point);
            Member member=memberRepository.findByEmail(email);
            member.setTotalPoint(member.getTotalPoint()+getPoint);
            memberRepository.save(member);
            challengeRepository.save(allChallenge);
        }
        return result;
    }

    @Override
    public String changeProfileImg(String profileImg, String email) {
        Member member=memberRepository.findByEmail(email);
        member.setProfileImg(profileImg);
        return profileImg;
    }

}