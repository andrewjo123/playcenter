package com.playground.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name="member_point")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MemberPoint extends BaseTimeEntity {
    @Id
    @Column(name="point_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ColumnDefault("0")
    int payPoint; // 포인트 변화

    @Column(name="point_email", nullable = false)
    String email;
}
