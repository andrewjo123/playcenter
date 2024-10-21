package com.playground.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCategoryDto {
    private Long id;

    // 추가
    @NotBlank(message = "플랫폼을 선택해주세요")
    private String company;

    @NotBlank(message = "장르를 선택해주세요")
    private String tag;

    private String editTag; // 유저가 정하는 게임장르?
}
