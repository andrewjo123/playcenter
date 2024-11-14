package com.playground.task;

import com.playground.entity.*;
import com.playground.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberRemoveTask {
    private final MemberRepository  memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DibsRepository dibsRepository;
    private final DibsItemRepository dibsItemRepository;
    private final MemberChallengeRepsoitory challengeRepository;
    private final MemberPointRepository pointRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final ReviewRepository reviewRepository;
    private final ItemCodeRepository itemCodeRepository;
    private final OrderRepository orderRepository;

    @Scheduled(cron = "59 59 23 31 12 *")
    @Transactional
    public void removeResignMember() throws Exception { //매년 말 최소 5년전에 회원 탈퇴한 유저 정보 삭제
        LocalDateTime baseDate = LocalDateTime.now().minusYears(5);
        System.out.println("Start deleting resign members...");
        List<Member> members=memberRepository.getResignMemberForDelete(baseDate);

        if(!members.isEmpty()){
            members.forEach(m->{
                System.out.println("target: "+m.getEmail());
                //장바구니제거
                Cart cart=cartRepository.findByMemberId(m.getId());
                List<CartItem> cartItems=cartItemRepository.findCartItemForDelete(cart.getId());
                if(!cartItems.isEmpty()){
                    cartItemRepository.deleteAll(cartItems);
                }
                cartRepository.delete(cart);

                //찜제거
                Dibs dibs=dibsRepository.findByMemberId(m.getId());
                List<DibsItem> dibsItems=dibsItemRepository.findDibsItemForDelete(dibs.getId());
                if(!dibsItems.isEmpty()){
                    dibsItemRepository.deleteAll(dibsItems);
                }
                dibsRepository.delete(dibs);

                //도전과제 제거
                challengeRepository.delete(challengeRepository.findByMemeberEmail(m.getEmail()));

                //포인트 제거
                List<MemberPoint> points=pointRepository.findByEmail(m.getEmail());
                if(!points.isEmpty()){
                    pointRepository.deleteAll(points);
                }

                //비밀번호 초기화 제거
                Optional<PasswordReset> pass=passwordResetRepository.findByMember_Id(m.getId());
                pass.ifPresent(passwordResetRepository::delete);

                //리뷰제거
                List<Review> reviews=reviewRepository.findByEmail(m.getEmail());
                if(!reviews.isEmpty()){
                    reviewRepository.deleteAll(reviews);
                }

                //구매코드 제거
                List<ItemCode> codes=itemCodeRepository.findByMember_Id(m.getId());
                if(!codes.isEmpty()){
                    itemCodeRepository.deleteAll(codes);
                }

                // 주문 제거
                List<Order> orderList = orderRepository.findByMember_Id(m.getId());
                if (!orderList.isEmpty()) {
                    orderRepository.deleteAll(orderList);
                }

                //유저 제거
                memberRepository.deleteById(m.getId());
            });
        }
    }
}
