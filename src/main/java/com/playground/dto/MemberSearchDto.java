package com.playground.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class MemberSearchDto {

    private String searchBy;      // 검색 필드 (이름, 이메일, 전화번호 등)
    private String searchQuery;   // 검색어
    private Boolean searchStatus; // 회원 상태 (활성/비활성)
}