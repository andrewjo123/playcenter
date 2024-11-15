package com.playground.repository;

import com.playground.entity.Dibs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DibsRepository extends JpaRepository<Dibs, Long> {
    Dibs findByMemberId(Long memberId);

    @Query("select d from Dibs d join Member m on d.member=m where m.email=:email")
    Dibs findByEmail(@Param("email") String email);
}
