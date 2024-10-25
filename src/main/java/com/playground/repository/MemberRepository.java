package com.playground.repository;

import com.playground.dto.MemberSearchDto;
import com.playground.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> , QuerydslPredicateExecutor<Member>, MemberRepositoryCustom  {

    Member findByEmail(String email);

    @Query("select m.email from Member m where m.name=:name and m.phone=:phone")
    Object[] findEmail(@Param("name") String name, @Param("phone")String phone);

    Optional<Member> findByEmailAndFromSocial(String email, boolean fromSocial);
    //추가
    @Query("select m from Member m join Order o on o.member=m where o.id = :orderId")
    Member findBuyer(@Param("orderId") Long orderId);
    //추가 1023 1330
    Optional<Member> findByEmailAndResign(String email, boolean resign);
}