package com.playground.service;

import com.playground.dto.CartDetailDto;
import com.playground.dto.CartItemDto;
import com.playground.dto.CartOrderDto;

import java.util.List;

public interface CartService {
    Long addCart(CartItemDto cartItemDto, String email);

    List<CartDetailDto> getCartList(String email);

    boolean validateCartItem(Long cartItemId, String email);

    void updateCartItemCount(Long cartItemId, int count);

    void deleteCartItem(Long cartItemId);

    Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email);

    //1024추가
    int getCartCount(String email);

    // 1111추가
    List<String> checkBeforeOrder(List<CartOrderDto> cartOrderDtoList);
}
