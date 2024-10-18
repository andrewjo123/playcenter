package com.playground.repository;

import com.playground.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailRepository extends JpaRepository<Email, Long> {
    Email findByEmail(String email);
    void deleteByEmail(String email);
}
