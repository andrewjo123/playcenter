package com.playground.dto;

import com.playground.constant.OrderStatus;
import com.playground.entity.Order;
import lombok.Getter;
import lombok.Setter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class OrderHistDto {

    public OrderHistDto(Order order){
        this.orderId = order.getId();
        this.orderDate = order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        this.orderStatus = order.getOrderStatus();
        this.sendCode=order.isSendCode();
        this.totalPrice=order.getTotalPrice();
    }

    private Long orderId; //주문아이디
    private String orderDate; //주문날짜
    private OrderStatus orderStatus; //주문 상태
    private int totalPrice;// 주문금액(아이템가격*아이템수);

    private List<OrderItemDto> orderItemDtoList = new ArrayList<>();

    //주문 상품리스트
    public void addOrderItemDto(OrderItemDto orderItemDto){
        orderItemDtoList.add(orderItemDto);
    }


    private boolean isPayed;// 결제여부

    //추가
    private boolean sendCode;// 코드 발송 여부

    private MemberPointDto pointDto;
}