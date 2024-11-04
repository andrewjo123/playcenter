package com.playground.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="password_reset")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PasswordReset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private Member member;

    private LocalDateTime expirationTime; // 만료 시간

    public PasswordReset(String token, Member member) {
        this.token = token;
        this.member = member;
        this.expirationTime = LocalDateTime.now().plusMinutes(10); // 10분 후 만료
    }
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationTime);
    }
}
