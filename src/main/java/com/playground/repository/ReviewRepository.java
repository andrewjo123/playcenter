package com.playground.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.playground.entity.Item;
import com.playground.entity.Review;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"recommendedMembers","member"})
    Page<Review> findByItem(Item item, Pageable pageable);  // 페이징 적용

    // 리뷰등록은 한게임당 한번만
    @Query("select r from Review r where r.item.id=:itemId and r.member.id=:memberId")
    List<Review> findByItemAndMember(@Param("itemId") Long itemId, @Param("memberId") Long memberId);

    @Query(value = "select r.* from review r join member m on m.member_id=r.member_id where m.email=:email order by r.update_time desc", nativeQuery = true)
    List<Review> findByMemberEmail(@Param("email") String email);

    // Task, 탈퇴한 유저의 리뷰기록 찾기
    @Query("select r from Review r where r.member.email=:email")
    List<Review> findByEmail(String email);
}
