package com.playground.repository;

import com.playground.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("select o from Order o " +
            "where o.member.email = :email and o.isPayed=true and o.orderDate >= :startDate " +
            "order by o.orderDate desc"
    )
    List<Order> findOrders(@Param("email") String email, @Param("startDate") LocalDateTime startDate, Pageable pageable);

    @Query("select count(o) from Order o " +
            "where o.member.email = :email and o.isPayed=true and o.orderDate >= :startDate "
    )
    Long countOrder(@Param("email") String email, @Param("startDate") LocalDateTime startDate);

    //추가
    @Query("select o from Order o " +
            "where o.id = :orderId "
    )
    List<Order> payOrder(@Param("orderId") Long orderId);
}