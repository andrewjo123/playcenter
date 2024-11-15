package com.playground.repository;

import com.playground.entity.ItemCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemCodeRepository extends JpaRepository<ItemCode, Long> {

    @Query(value = "SELECT * FROM item_code i WHERE i.item_id=:itemId and i.member_id IS NULL LIMIT :count", nativeQuery = true)
    List<ItemCode> getCode(@Param("itemId") Long itemId,@Param("count") int count);

    @Query(value = "SELECT * FROM item_code i WHERE i.item_id = :itemId AND i.member_id = :memberId", nativeQuery = true)
    List<ItemCode> findByItemIdAndMemberId(@Param("itemId") Long itemId, @Param("memberId") Long memberId);

    // Task, 탈퇴한 유저의 구매코드 찾기
    List<ItemCode> findByMember_Id(Long id);
}
