package com.playground.repository;

import com.playground.dto.CartDetailDto;
import com.playground.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByCartIdAndItemId(Long cartId, Long itemId);

    @Query("select new com.playground.dto.CartDetailDto(ci.id, i.id ,i.itemNm, i.price, ci.count, im.imgUrl) " +
            "from CartItem ci, ItemImg im " +
            "join ci.item i " +
            "where ci.cart.id = :cartId " +
            "and im.item.id = ci.item.id " +
            "and im.repimgYn = 'Y' " +
            "order by ci.regTime desc"
            )
    List<CartDetailDto> findCartDetailDtoList(Long cartId);

    long countByCartId(Long cartId);

    // Task, 탈퇴한 유저의 장바구니 아이템 찾기
    @Query("select ci from CartItem ci where ci.cart.id=:cartId")
    List<CartItem> findCartItemForDelete(Long cartId);
}