package com.playground.dto;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCategoryDto {
    private Long id;

    private String company; //회사 카테고리

    private String tag; // 게임 장르

    private String editTag; // 유저가 정하는 게임장르?
}
