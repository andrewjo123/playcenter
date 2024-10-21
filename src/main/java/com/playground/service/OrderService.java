package com.playground.service;

import com.playground.dto.OrderDto;
import com.playground.dto.OrderHistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    Long order(OrderDto orderDto, String email);

    Page<OrderHistDto> getOrderList(String email, Pageable pageable);

    boolean validateOrder(Long orderId, String email);

    void cancelOrder(Long orderId);

    Long orders(List<OrderDto> orderDtoList, String email);

    //추가
    List<OrderHistDto> getPayList(Long orderId);
    String findBuyer(Long orderId);
    String validpay(Long orderId, Long totalPrice);
    void payedOrder(Long orderId);
    void removeList(Long orderId);
}
