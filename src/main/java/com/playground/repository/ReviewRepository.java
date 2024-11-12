package com.playground.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.playground.entity.Item;
import com.playground.entity.Member;
import com.playground.entity.Review;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @EntityGraph(attributePaths = {"member"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<Review> findByItem(Item item, Pageable pageable);  // 페이징 적용


    @Modifying
    @Query("delete from Review mr where mr.member = :member")
    void deleteByMember(Member member);

    @EntityGraph(attributePaths = {"member"}, type = EntityGraph.EntityGraphType.FETCH)
    Page<Review> findAll(Pageable pageable);

    // 특정 아이템에 연관된 모든 리뷰 삭제
    @Modifying
    @Transactional
    @Query("delete from Review r where r.item.id = :itemId")
    void deleteByItemId(Long itemId);

    // 리뷰등록은 한게임당 한번만
    @Query("select r from Review r where r.item.id=:itemId and r.member.id=:memberId")
    List<Review> findByItemAndMember(@Param("itemId") Long itemId, @Param("memberId") Long memberId);

    @Query(value = "select r.* from Review r join member m on m.member_id=r.member_id where m.email=:email order by r.update_time desc", nativeQuery = true)
    List<Review> findByMemberEmail(@Param("email") String email);
}
