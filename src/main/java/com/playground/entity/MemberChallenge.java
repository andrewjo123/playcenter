package com.playground.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name="member_challenge")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberChallenge extends BaseTimeEntity {
    @Id
    @Column(name="challenge_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ColumnDefault("0")
    private int usedTotalMoney; // 사용한 총 금액

    @ColumnDefault("0")
    private int achievementForMoney;

    @ColumnDefault("0")
    private int countForSteam;

    @ColumnDefault("0")
    private int achievementForSteam;

    @ColumnDefault("0")
    private int countForNintendo;

    @ColumnDefault("0")
    private int achievementForNintendo;

    @ColumnDefault("0")
    private int countForPs;

    @ColumnDefault("0")
    private int achievementForPs;
}
