package com.playground.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberDetailDto {

    private Long memberId;

    private String name;

    private String email;

    private String phone;

    private String addressCode;

    private String address;

    private String addressDetail;

    private boolean fromSocial;

    private boolean resign; // 활성화/비활성화 상태를 나타냄

    private String role;

    private String regTime;

    private String updateTime;
}