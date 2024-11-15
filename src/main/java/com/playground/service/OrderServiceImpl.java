package com.playground.service;

import com.playground.dto.MemberPointDto;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final MemberPointRepository pointRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberChallengeRepsoitory challengeRepository;
    private final ItemCategoryRepository categoryRepository;
    private final CartRepository cartRepository;

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
    public Page<OrderHistDto> getOrderList(String email, int date, Pageable pageable) {
        LocalDateTime startDate=LocalDateTime.now().minusMonths(date-1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Order> orders = orderRepository.findOrders(email, startDate, pageable);
        Long totalCount = orderRepository.countOrder(email, startDate);

        List<OrderHistDto> orderHistDtos = new ArrayList<>();
        for (Order order : orders) {
            OrderHistDto orderHistDto = new OrderHistDto(order);
            List<OrderItem> orderItems = order.getOrderItems();

            for (OrderItem orderItem : orderItems) {
                ItemImg itemImg = itemImgRepository.findByItemIdAndRepimgYn(orderItem.getItem().getId(), "Y");
                OrderItemDto orderItemDto = new OrderItemDto(orderItem, itemImg.getImgUrl());
                orderHistDto.addOrderItemDto(orderItemDto);
            }
            MemberPoint point=pointRepository.findOldestOne(order.getId());
            MemberPointDto pointDto=new MemberPointDto();
            if(point!=null){
                pointDto.setUsedPoint(Math.abs(point.getPayPoint()));
                pointDto.setStackPoint((int) ((order.getTotalPrice() - Math.abs(point.getPayPoint())) * 0.01));
            } else{
                pointDto.setUsedPoint(0);
                pointDto.setStackPoint(0);
            }
            orderHistDto.setPointDto(pointDto);

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
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(EntityNotFoundException::new);
        order.cancelOrder();
        Member member=order.getMember();
        MemberPoint memberPoint=pointRepository.findByOrder_Id(order.getId()).orElseThrow(EntityNotFoundException::new);
        member.setTotalPoint(member.getTotalPoint()-memberPoint.getPayPoint());
        MemberPoint returnPoint=new MemberPoint();
        returnPoint.setOrder(order);
        returnPoint.setPayPoint(-memberPoint.getPayPoint());
        returnPoint.setEmail(member.getEmail());

        //1029추가
        List<Item> items = new ArrayList<>();
        order.getOrderItems().forEach(oItem -> {
            Item item = oItem.getItem();
            item.setStockNumber(item.getStockNumber() + oItem.getCount());
            items.add(item);
        });

        itemRepository.saveAll(items);
        pointRepository.save(returnPoint);
        memberRepository.save(member);
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
    public String[] findBuyer(Long orderId) {
        Member member=memberRepository.findBuyer(orderId);
        return new String[]{member.getName(), String.valueOf(member.getTotalPoint())};
    }

    @Override
    public String validpay(Long orderId, Long totalPrice, Long usePoint) {
        List<Order> orders = orderRepository.payOrder(orderId);
        Member member=orders.get(0).getMember();
        if(usePoint>member.getTotalPoint()){
            return "not";
        }

        Long total=0L;
        for (Order order : orders) {
            total += order.getTotalPrice();
        }

        String validation="";
        if(total.equals(totalPrice+usePoint)){
            validation="ok";
        } else{
            validation="not";
        }

        return validation;
    }

    @Transactional
    @Override
    public void payedOrder(Long orderId, Long usePoint) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        order.setPayed(true);
        Member member=order.getMember();
        member.setTotalPoint((int) (member.getTotalPoint()-usePoint));
        MemberPoint memberPoint=new MemberPoint();
        memberPoint.setOrder(order);
        memberPoint.setPayPoint((int)-usePoint);
        memberPoint.setEmail(member.getEmail());

        //1029추가
        List<Item> items = new ArrayList<>();
        order.getOrderItems().forEach(oItem -> {
            Item item = oItem.getItem();
            item.setStockNumber(item.getStockNumber() - oItem.getCount());
            items.add(item);
        });

        itemRepository.saveAll(items);
        orderRepository.save(order);
        memberRepository.save(member);
        pointRepository.save(memberPoint);
    }

    @Override
    public void removeList(Long orderId) {
        orderRepository.deleteById(orderId);
    }

    //1025 1740수정
    @Override
    @Transactional
    public String sendAllCodes(Long orderId, String email) {
        String result = "";
        AtomicInteger totalPrice=new AtomicInteger(0);

        Member member1=memberRepository.findByEmail(email);
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent()){
            Map<List<String>, List<String>> sendCodeLists=new LinkedHashMap<>();
            List<OrderItem> orderItems = order.get().getOrderItems();
            List<ItemCode> gatherCodes = new ArrayList<>();
            Map<String,Integer> companyCount=new HashMap<>();
            orderItems.forEach(oItem ->{
                int count=oItem.getCount();
                totalPrice.addAndGet(oItem.getTotalPrice());
                List<ItemCode> codeList=codeRepository.getCode(oItem.getItem().getId(),count);
                List<String> itemNameAndImg=new ArrayList<>();
                itemNameAndImg.add(0,oItem.getItem().getItemNm());
                itemNameAndImg.add(1,itemImgRepository.findByItemId(oItem.getItem().getId()).get(0).getImgName());
                List<String> sendCodes=new ArrayList<>();
                codeList.forEach(code1->{
                    sendCodes.add(code1.getCodNum());
                    code1.setMember(member1);
                    gatherCodes.add(code1);
                });
                sendCodeLists.put(itemNameAndImg,sendCodes);
                String company=categoryRepository.findByItemId(oItem.getItem().getId()).getCompany();
                if(company.equals("steam")){
                    companyCount.put("steam",companyCount.getOrDefault("steam", 0)+count);
                }
                if(company.equals("nintendo")){
                    companyCount.put("nintendo",companyCount.getOrDefault("nintendo", 0)+count);
                }
                if(company.equals("ps")){
                    companyCount.put("ps",companyCount.getOrDefault("ps", 0)+count);
                }
                Item originItem=oItem.getItem();
                originItem.setBuyCnt(originItem.getBuyCnt()+count);
                itemRepository.save(originItem);
            });
            //이메일 발송 로직구현
            codeRepository.saveAll(gatherCodes);
            order.get().setSendCode(true);
            orderRepository.save(order.get());
            int payPoint=pointRepository.findOldestOne(order.get().getId()).getPayPoint();
            //포인트 적립
            if((int)((totalPrice.get()-payPoint)*0.01)>0){
                MemberPoint point= new MemberPoint();
                point.setPayPoint((int)(totalPrice.get()*0.01));
                point.setOrder(order.get());
                point.setEmail(email);
                pointRepository.save(point);
                member1.setTotalPoint(member1.getTotalPoint()+(int)(totalPrice.get()*0.01));
                memberRepository.save(member1);
            }
            //도전과제
            if(totalPrice.get()-payPoint>0){
                MemberChallenge challenge=challengeRepository.findByMemeberEmail(member1.getEmail());
                challenge.setUsedTotalMoney(challenge.getUsedTotalMoney()+totalPrice.get()-payPoint);
                challenge.setCountForSteam(challenge.getCountForSteam() + companyCount.getOrDefault("steam", 0));
                challenge.setCountForNintendo(challenge.getCountForNintendo() + companyCount.getOrDefault("nintendo", 0));
                challenge.setCountForPs(challenge.getCountForPs() + companyCount.getOrDefault("ps", 0));
                challengeRepository.save(challenge);
            }

            emailService.sendAllCodes(email, "[놀이마당] 게임코드 발송", "mailForm/sendGameCodes",sendCodeLists);
            result="success";
        } else{
            result="none";
        }
        return result;
    }

    @Override
    public List<String> checkStack(Map<String, String> cartIds) {
        List<String> result=new ArrayList<>();
        cartIds.forEach((key, value) -> {
            Optional<CartItem> cartItem=cartItemRepository.findById(Long.valueOf(value));
            if(cartItem.get().getCount()>cartItem.get().getItem().getStockNumber()){
                result.add(cartItem.get().getItem().getItemNm());
            }
        });
        if (result.isEmpty()) {
            result.add("conTinueForPay");
        }
        return result;
    }

    @Override
    public List<String> checkStackById(Long orderId) {
        Order order=orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        List<String> result=new ArrayList<>();
        order.getOrderItems().forEach(oItem->{
            if(oItem.getCount()>oItem.getItem().getStockNumber()){
                result.add(oItem.getItem().getItemNm());
            }
        });
        if (result.isEmpty()) {
            result.add("conTinueForPay");
        }
        return result;
    }

    @Override
    public Long checkStackFromItemDtl(Long itemId, int count, String email) {
        Item item=itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);
        if(item.getStockNumber()>=count){
            CartItem cartItem=cartItemRepository.findByCartIdAndItemId(cartRepository.findByEmail(email).getId(),itemId);
            if(cartItem!=null){
                cartItemRepository.delete(cartItem);
            }
            return item.getId();
        } else{
            return 0L;
        }
    }

}
