package com.playground.repository;

import com.playground.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

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

    @Query(value="SELECT m.email FROM member m JOIN dibs d ON m.member_id=d.member_id JOIN dibs_item di ON di.dibs_id=d.dibs_id WHERE di.item_id=:itemId", nativeQuery = true)
    List<Object> findEmailFromItemId(Long itemId);

    // Task, 5년전 회원탈퇴한 멤버 찾기
    @Query("select m from Member m " +
            "where m.resign=true and m.updateTime <= :baseDate ")
    List<Member> getResignMemberForDelete(@Param("baseDate") LocalDateTime baseDate);
}