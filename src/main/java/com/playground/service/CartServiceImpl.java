package com.playground.service;

import com.playground.dto.CartDetailDto;
import com.playground.dto.CartItemDto;
import com.playground.dto.CartOrderDto;
import com.playground.dto.OrderDto;
import com.playground.entity.Cart;
import com.playground.entity.CartItem;
import com.playground.entity.Item;
import com.playground.entity.Member;
import com.playground.repository.CartItemRepository;
import com.playground.repository.CartRepository;
import com.playground.repository.ItemRepository;
import com.playground.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderService orderService;

    @Override
    public Long addCart(CartItemDto cartItemDto, String email) {
        Item item = itemRepository.findById(cartItemDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);
        Member member = memberRepository.findByEmail(email);

        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }
        if(cartItemDto.getCount()>item.getStockNumber()){
            return 0L;
        }

        CartItem savedCartItem = cartItemRepository.findByCartIdAndItemId(cart.getId(), item.getId());

        if (savedCartItem != null) {
            if(savedCartItem.getCount()+cartItemDto.getCount()>item.getStockNumber()){
                return 0L;
            }
            savedCartItem.addCount(cartItemDto.getCount());
            return savedCartItem.getId();
        } else {
            CartItem cartItem = CartItem.createCartItem(cart, item, cartItemDto.getCount());
            cartItemRepository.save(cartItem);
            return cartItem.getId();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<CartDetailDto> getCartList(String email) {
        List<CartDetailDto> cartDetailDtoList = new ArrayList<>();

        Member member = memberRepository.findByEmail(email);
        Cart cart = cartRepository.findByMemberId(member.getId());
        if (cart == null) {
            return cartDetailDtoList;
        }

        cartDetailDtoList = cartItemRepository.findCartDetailDtoList(cart.getId());
        return cartDetailDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public boolean validateCartItem(Long cartItemId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = cartItem.getCart().getMember();

        return StringUtils.equals(curMember.getEmail(), savedMember.getEmail());
    }

    @Override
    public void updateCartItemCount(Long cartItemId, int count) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);

        cartItem.updateCount(count);
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(EntityNotFoundException::new);
        cartItemRepository.delete(cartItem);
    }

    @Override
    public Long orderCartItem(List<CartOrderDto> cartOrderDtoList, String email) {
        List<OrderDto> orderDtoList = new ArrayList<>();

        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository
                    .findById(cartOrderDto.getCartItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(cartItem.getItem().getId());
            orderDto.setCount(cartItem.getCount());
            orderDtoList.add(orderDto);
        }

        Long orderId = orderService.orders(orderDtoList, email);
//        for (CartOrderDto cartOrderDto : cartOrderDtoList) {
//            CartItem cartItem = cartItemRepository
//                    .findById(cartOrderDto.getCartItemId())
//                    .orElseThrow(EntityNotFoundException::new);
//            cartItemRepository.delete(cartItem);
//        }

        return orderId;
    }

    // 1024추가
    @Override
    public int getCartCount(String email) {
        Long cartId=cartRepository.findByEmail(email).getId();
        return (int)cartItemRepository.countByCartId(cartId);
    }

    // 1111추가
    @Override
    public List<String> checkBeforeOrder(List<CartOrderDto> cartOrderDtoList) {
        List<String> result=new ArrayList<>();
        cartOrderDtoList.forEach((cartOrder)->{
            Optional<CartItem> cartItem=cartItemRepository.findById(cartOrder.getCartItemId());
            if(cartItem.get().getCount()>cartItem.get().getItem().getStockNumber()){
                result.add(cartItem.get().getItem().getItemNm());
            }
        });
        if (result.isEmpty()) {
            result.add("conTinueForPay");
        }
        return result;
    }
}
