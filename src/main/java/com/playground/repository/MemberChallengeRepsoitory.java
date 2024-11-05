package com.playground.repository;

import com.playground.entity.MemberChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemberChallengeRepsoitory extends JpaRepository <MemberChallenge,Long> {
    @Query("select mc from MemberChallenge mc join Member m on mc.member=m where m.email=:email")
    MemberChallenge findByMemeberEmail(String email);
}
