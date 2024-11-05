package com.playground.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PointHistDto {
    private LocalDateTime useDate;
    private int payPoint;
    private String orderStatus;
}
