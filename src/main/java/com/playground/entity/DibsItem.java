package com.playground.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="dibs_item")
public class DibsItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dibs_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="dibs_id")
    private Dibs dibs;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    public static com.playground.entity.DibsItem createDibsItem(Dibs dibs, Item item) {
        com.playground.entity.DibsItem dibsItem = new com.playground.entity.DibsItem();
        dibsItem.setDibs(dibs);
        dibsItem.setItem(item);
        return dibsItem;
    }

}
