package com.playground.task;

import com.playground.entity.Item;
import com.playground.entity.Order;
import com.playground.entity.OrderItem;
import com.playground.repository.ItemRepository;
import com.playground.repository.OrderItemRepository;
import com.playground.repository.OrderRepository;
import com.playground.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderRemoveTask {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderService orderService;

    // 매일 아침 7시 정각에 실행
    @Scheduled(cron = "0 0 07 * * *")
    @Transactional
    public void checkNotPayed() throws Exception { // 결제페이지에서 비정상적 종료가 된 경우 db에 정보가 남음, 정상화

        LocalDateTime baseDate = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Order> orderList = orderRepository.getNotPayedOrder(baseDate);

        System.out.println("Start deleting unpaid orders...");
        if (!orderList.isEmpty()) {
            orderRepository.deleteAll(orderList);
            System.out.println("Deleted orders: " + orderList);
            System.out.println("Delete operation completed.");
        } else {
            System.out.println("No unpaid orders to delete.");
        }
    }

    // 매일 아침 8시 정각에 실행
    @Scheduled(cron = "0 0 08 * * *")
    @Transactional
    public void checkNotConfirm() throws Exception { // 결제 후 장기간 코드발송 또는 취소를 누르지 않은 목록에 대해 결제 자동 취소

        LocalDateTime baseDate = LocalDateTime.now().minusDays(3).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Order> orderList = orderRepository.getNotConfirmLongTime(baseDate);

        System.out.println(orderList);
        System.out.println("Start deleting no confirmed orders...");
        if (!orderList.isEmpty()) {
            orderList.forEach(o->{
                orderService.cancelOrder(o.getId());
            });
            System.out.println("Delete operation completed.");
        } else {
            System.out.println("No confirmed orders to delete.");
        }
    }
}
