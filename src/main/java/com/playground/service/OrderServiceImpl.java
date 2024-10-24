package com.playground.service;

import com.playground.dto.OrderDto;
import com.playground.dto.OrderHistDto;
import com.playground.dto.OrderItemDto;
import com.playground.entity.*;
import com.playground.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final ItemImgRepository itemImgRepository;
    private final ItemCodeRepository codeRepository;
    private final EmailService emailService;

    @Override
    public Long order(OrderDto orderDto, String email) {
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);

        Member member = memberRepository.findByEmail(email);

        List<OrderItem> orderItemList = new ArrayList<>();
        OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
        orderItemList.add(orderItem);
        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OrderHistDto> getOrderList(String email, Pageable pageable) {
        List<Order> orders = orderRepository.findOrders(email, pageable);
        Long totalCount = orderRepository.countOrder(email);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();

        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }

            orderHistDtos.add(orderHistDto);
        }

        return new PageImpl<>(orderHistDtos, pageable, totalCount);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean validateOrder(Long orderId, String email) {
        Member curMember = memberRepository.findByEmail(email);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        Member savedMember = order.getMember();

        return StringUtils.equals(curMember.getEmail(), savedMember.getEmail());
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();
    }

    @Override
    public Long orders(List<OrderDto> orderDtoList, String email) {
        Member member = memberRepository.findByEmail(email);
        List<OrderItem> orderItemList = new ArrayList<>();

        for (OrderDto orderDto : orderDtoList) {
            Item item = itemRepository.findById(orderDto.getItemId())
                    .orElseThrow(EntityNotFoundException::new);

            OrderItem orderItem = OrderItem.createOrderItem(item, orderDto.getCount());
            orderItemList.add(orderItem);
        }

        Order order = Order.createOrder(member, orderItemList);
        orderRepository.save(order);

        return order.getId();
    }

    //추가
    @Override
    public List<OrderHistDto> getPayList(Long orderId) {
        List<Order> orders = orderRepository.payOrder(orderId);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();
        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            orderHistDtos.add(orderHistDto);
        }
        return orderHistDtos;
    }

    @Override
    public String findBuyer(Long orderId) {
        return memberRepository.findBuyer(orderId).getName();
    }

    @Override
    public String validpay(Long orderId, Long totalPrice) {
        List<Order> orders = orderRepository.payOrder(orderId);
        Long total=0L;

        for (Order order : orders) {
            total += order.getTotalPrice();
        }
        String validation="";

        if(total.equals(totalPrice)){
            validation="ok";
        } else{
            validation="not";
        }

        return validation;
    }

    @Override
    public void payedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        order.setPayed(true);
        orderRepository.save(order);

    }

    @Override
    public void removeList(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    //1023 1740추가
    @Override
    @Transactional
    public String sendAllCodes(Long orderId, String email) {
        String result = "";
        StringBuilder mail_body=new StringBuilder();
        Member member1=memberRepository.findByEmail(email);
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()){
            List<OrderItem> orderItems = order.get().getOrderItems();
            List<ItemCode> gatherCodes = new ArrayList<>();
            orderItems.forEach(item ->{
                int count=item.getCount();
                List<ItemCode> codeList=codeRepository.getCode(count);
                codeList.forEach(code1->{
                    mail_body.append(item.getItem().getItemNm());
                    mail_body.append(": ").append(code1.getCodNum()).append("\n");
                    code1.setMember(member1);
                    gatherCodes.add(code1);
                });
            });
            //이메일 발송 로직구현
            codeRepository.saveAll(gatherCodes);
            order.get().setSendCode(true);
            orderRepository.save(order.get());
            emailService.sendEmail(email, "[놀이마당] 게임코드 발송", String.valueOf(mail_body));
            result="success";
        } else{
            result="none";
        }
        return result;
    }
}
