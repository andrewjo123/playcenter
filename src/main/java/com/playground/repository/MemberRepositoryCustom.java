package com.playground.repository;

import com.playground.dto.MemberSearchDto;
import com.playground.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryCustom {

    //조민 추가 - 회원 목록 불러오기
    Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable);
}
