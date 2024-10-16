package com.playground.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Getter
@Setter
public class ItemDto {

    private Long id;

    private String itemNm;

    private Integer price;

    private String itemDetail;

    private String sellStatCd;

    private LocalDateTime regTime;

    private LocalDateTime updateTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate openDate;

    private int buyCnt;

    //리뷰
    private double avg;

    private int reviewCnt;

    

}