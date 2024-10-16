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
}
