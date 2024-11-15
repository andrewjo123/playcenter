package com.playground.repository;

import com.playground.constant.Role;
import com.playground.dto.MemberSearchDto;
import com.playground.entity.Member;
import com.playground.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import java.util.List;

public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {

    private JPAQueryFactory queryFactory;

    public MemberRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    // 검색 조건: 이름, 이메일, 전화번호 등에 따른 검색
    private BooleanExpression searchByLike(String searchBy, String searchQuery) {
        if (StringUtils.isEmpty(searchQuery)) {
            return null;  // 검색어가 없으면 조건을 무시합니다.
        }
        if ("name".equalsIgnoreCase(searchBy)) {
            return QMember.member.name.like("%" + searchQuery + "%");
        } else if ("email".equalsIgnoreCase(searchBy)) {
            return QMember.member.email.like("%" + searchQuery + "%");
        } else if ("phone".equalsIgnoreCase(searchBy)) {
            return QMember.member.phone.like("%" + searchQuery + "%");
        }
        return null; // searchBy에 맞는 필드가 없으면 조건을 무시합니다.
    }

    // 활성화/비활성화 상태에 따른 필터링
    private BooleanExpression searchStatusEq(Boolean searchStatus) {
        if (searchStatus == null) {
            return null; // searchStatus가 null이면 조건을 무시합니다.
        }
        return QMember.member.resign.eq(!searchStatus); // true(비활성화), false(활성화)
    }


@Override
    public Page<Member> getAdminMemberPage(MemberSearchDto memberSearchDto, Pageable pageable) {

        // QueryDSL을 사용하여 필터링된 회원 목록을 가져옵니다.

        List<Member> content = queryFactory
                    .selectFrom(QMember.member)
                    .where(
                            searchStatusEq(memberSearchDto.getSearchStatus()),  // 활성화/비활성화 상태 필터
                            searchByLike(memberSearchDto.getSearchBy(), memberSearchDto.getSearchQuery()), // 통합 검색 필터
                            excludeAdminRole() // admin 역할 제외 필터
                    )
                    .orderBy(QMember.member.id.desc())  // 최신순으로 정렬
                    .offset(pageable.getOffset())  // 페이지네이션 오프셋 설정
                    .limit(pageable.getPageSize())  // 페이지 크기 설정
                    .fetch();  // 결과를 가져옵니다.

            // 총 회원 수를 계산합니다.
            long total = queryFactory
                    .select(Wildcard.count)
                    .from(QMember.member)
                    .where(
                            searchStatusEq(memberSearchDto.getSearchStatus()),
                            searchByLike(memberSearchDto.getSearchBy(), memberSearchDto.getSearchQuery()),
                            excludeAdminRole() // admin 역할 제외 필터
                    )
                    .fetchOne();

            // 결과를 Page 객체로 반환합니다.
            return new PageImpl<>(content, pageable, total);
    }

    // 회원 이름 검색 조건
    private BooleanExpression memberNameLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QMember.member.name.like("%" + searchQuery + "%");
    }

    // 이메일 검색 조건
    private BooleanExpression emailLike(String searchQuery) {
        return StringUtils.isEmpty(searchQuery) ? null : QMember.member.email.like("%" + searchQuery + "%");
    }

    // 활성화/비활성화 상태 검색 조건
    private BooleanExpression resignStatusEq(Boolean resign) {
        return resign == null ? null : QMember.member.resign.eq(resign);
    }

    // admin 역할을 제외하는 조건
    private BooleanExpression excludeAdminRole() {
        return QMember.member.role.ne(Role.ADMIN);
    }


}
