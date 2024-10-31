package com.playground.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DibsDto {
    private Long itemId; // Item의 id

    private Long dibsItemId; // DibsItem의 id

    private String itemNm;

    private int price;

    private String imgUrl;

    public DibsDto(Long itemId, Long dibsItemId, String itemNm, int price,  String imgUrl){
        this.itemId=itemId;
        this.dibsItemId = dibsItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
    }
}
