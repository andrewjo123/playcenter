package com.playground.repository;

import com.playground.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByEmail(String email);

    @Query("select m.email from Member m where m.name=:name and m.phone=:phone")
    Object[] findEmail(@Param("name") String name, @Param("phone")String phone);

}