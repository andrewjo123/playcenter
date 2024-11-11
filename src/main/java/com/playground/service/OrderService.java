package com.playground.service;

import com.playground.dto.OrderDto;
import com.playground.dto.OrderHistDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface OrderService {
    Long order(OrderDto orderDto, String email);

    Page<OrderHistDto> getOrderList(String email, int date, Pageable pageable);

    boolean validateOrder(Long orderId, String email);

    void cancelOrder(Long orderId);

    Long orders(List<OrderDto> orderDtoList, String email);

    List<OrderHistDto> getPayList(Long orderId);
    String[] findBuyer(Long orderId);
    String validpay(Long orderId, Long totalPrice, Long usePoint);
    void payedOrder(Long orderId, Long usePoint);
    void removeList(Long orderId);

    String sendAllCodes(Long orderId, String email);

    List<String> checkStack(Map<String, String> cartIds);
    List<String> checkStackById(Long orderId);
    Long checkStackFromItemDtl(Long itemId, int count, String email);
}
