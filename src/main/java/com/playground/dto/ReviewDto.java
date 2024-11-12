package com.playground.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReviewDto {

    private Long id; //상품코드

    @NotNull
    private int grade; // 평점, 1~5

    @NotBlank
    private String text; // 리뷰 내용

    @NotNull
    private int rCnt; // 리뷰 추천수

    //review num
    private Long reviewnum;

    //Membmer id
    private Long member_id;
    
    //Member email
    private String email;
    

    private LocalDateTime regTime, updateTime;

    private boolean isRecommended; // 추천 여부 필드 추가

    private String profileImg;

    private String itemNm;
}
