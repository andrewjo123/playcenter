package com.playground.repository;

import com.playground.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Cart findByMemberId(Long memberId);

    @Query("select c from Cart c join Member m on c.member=m where m.email=:email")
    Cart findByEmail(@Param("email") String email);
}