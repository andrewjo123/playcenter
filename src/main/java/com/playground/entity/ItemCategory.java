package com.playground.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "item_id")
    private Item item; // FK from Item

    //1025추가
    @ColumnDefault("false")
    private boolean action;
    @ColumnDefault("false")
    private boolean adventure;
    @ColumnDefault("false")
    private boolean rpg;
    @ColumnDefault("false")
    private boolean shooter;
    @ColumnDefault("false")
    private boolean strategy;
    @ColumnDefault("false")
    private boolean simulation;
    @ColumnDefault("false")
    private boolean puzzle;
    @ColumnDefault("false")
    private boolean sports;
    @ColumnDefault("false")
    private boolean racing;
    @ColumnDefault("false")
    private boolean fighting;
    @ColumnDefault("false")
    private boolean survival;
    @ColumnDefault("false")
    private boolean rhythm;
    @ColumnDefault("false")
    private boolean sandbox;
    @ColumnDefault("false")
    private boolean battleRoyale;
    @ColumnDefault("false")
    private boolean card;
    @ColumnDefault("false")
    private boolean boardGame;
    @ColumnDefault("false")
    private boolean horror;
    @ColumnDefault("false")
    private boolean platformer;
    @ColumnDefault("false")
    private boolean moba;
    @ColumnDefault("false")
    private boolean mmorpg;
}
