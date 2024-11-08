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

    private int stockNumber;

    public DibsDto(Long itemId, Long dibsItemId, String itemNm, int price, String imgUrl, int stockNumber) {
        this.itemId = itemId;
        this.dibsItemId = dibsItemId;
        this.itemNm = itemNm;
        this.price = price;
        this.imgUrl = imgUrl;
        this.stockNumber = stockNumber; // 추가된 필드
    }

    public boolean isSoldOut() {
        return stockNumber <= 0;
    }

    public void setSoldOut(boolean soldOut) {
    }
}
