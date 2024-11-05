package com.playground.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MainItemDto {

    private Long id;

    private String itemNm;

    private String itemDetail;

    private String imgUrl;

    private Integer price;

    private Integer stockNumber;

    private Integer buyCnt;

    @QueryProjection
    public MainItemDto(Long id, String itemNm, String imgUrl,Integer price, Integer stockNumber, Integer buyCnt){
        this.id = id;
        this.itemNm = itemNm;
        this.imgUrl = imgUrl;
        this.price = price;
        this.stockNumber= stockNumber;
        this.buyCnt=buyCnt;
    }

}