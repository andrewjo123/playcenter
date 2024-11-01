package com.playground.repository;

import com.playground.entity.MemberPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberPointRepository extends JpaRepository<MemberPoint, Long> {
    Optional<MemberPoint> findByOrder_Id(Long orderId);

    @Query(value = "select p.* from member_point p where p.order_id = :orderId order by p.reg_time LIMIT 1", nativeQuery = true)
    MemberPoint findOldestOne(@Param("orderId") Long orderId);

    @Query(value="SELECT mp.reg_time, mp.pay_point, o.order_status FROM member_point mp JOIN orders o ON o.order_id=mp.order_id JOIN member m ON o.member_id=m.member_id WHERE m.email=:email AND mp.reg_time >= NOW() - INTERVAL :date MONTH AND mp.pay_point != 0 ORDER BY mp.reg_time desc", nativeQuery = true)
    List<Object[]> getPointHistory(@Param("email") String email, @Param("date") int date);
}
