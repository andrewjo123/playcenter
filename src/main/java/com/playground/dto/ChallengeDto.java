package com.playground.dto;

import com.playground.entity.MemberChallenge;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

@Getter
@Setter
public class ChallengeDto {

    private String email;

    // 결제 금액 업적
    private int usedTotalMoney; // 사용한 총 금액

    private int achievementForMoney;

    // 회사별 구매 업적
    private int countForSteam;

    private int achievementForSteam;

    private int countForNintendo;

    private int achievementForNintendo;

    private int countForPs;

    private int achievementForPs;

    private static ModelMapper modelMapper = new ModelMapper();

    public MemberChallenge createChallenge() {
        return modelMapper.map(this, MemberChallenge.class);
    }

    public static ChallengeDto of(MemberChallenge memberChallenge) {
        return modelMapper.map(memberChallenge, ChallengeDto.class);
    }
}
