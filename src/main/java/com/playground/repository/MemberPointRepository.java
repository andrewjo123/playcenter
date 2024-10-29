package com.playground.repository;

import com.playground.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {
    Optional<MemberPoint> findByOrder_Id(Long orderId);

    @Query(value = "select p.* from member_point p where p.order_id = :orderId order by p.reg_time LIMIT 1", nativeQuery = true)
    MemberPoint findOldestOne(@Param("orderId") Long orderId);

}
