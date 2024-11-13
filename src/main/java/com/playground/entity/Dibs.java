package com.playground.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "dibs")
@Getter
@Setter
@ToString
public class Dibs {

    @Id
    @Column(name = "dibs_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    public static Dibs createDibs(Member member){
        Dibs dibs = new Dibs();
        dibs.setMember(member);
        return dibs;
    }

}
