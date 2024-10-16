package com.playground.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="item_category")
@Getter
@Setter
@ToString
public class ItemCategory {
    @Id
    @Column(name="item_category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String company; //회사 카테고리

    @Column(nullable = false)
    private String tag; // 게임 장르

    @Column(nullable = false)
    private String editTag; // 유저가 정하는 게임장르?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Item item; // FK from Item
}
