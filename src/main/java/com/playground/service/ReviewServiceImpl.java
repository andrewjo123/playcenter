package com.playground.service;


import com.playground.entity.ItemCode;
import com.playground.repository.ItemCodeRepository;
import com.playground.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.playground.dto.ReviewDto;
import com.playground.entity.Item;
import com.playground.entity.Member;
import com.playground.entity.Review;
import com.playground.repository.MemberRepository;
import com.playground.repository.ReviewRepository;

import java.util.Optional;

@Service
@Log4j2
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final ItemCodeRepository codeRepository;

    //add paging
    @Override
    public Page<ReviewDto> getListOfItem(Long id, Pageable pageable, String currentUserEmail) {
        Item item = Item.builder().id(id).build();
        log.info("Fetching reviews for item: {}", id);

        // 페이징을 적용하여 리뷰 리스트를 반환
        Page<Review> result = reviewRepository.findByItem(item, pageable);

        // return result.stream().map(itemReview->entityToDto(itemReview)).collect(Collectors.toList());
        return result.map(review -> entityToDto(review, currentUserEmail));  // Stream 대신 Page의 map 메소드 사용
        
    }

    @Override
    public Long register(ReviewDto itemReviewDto) {
        String email=itemReviewDto.getEmail();
        Long memId=memberRepository.findByEmail(email).getId();
        itemReviewDto.setMember_id(memId);
        Review itemReview=dtoToEntity(itemReviewDto);
        reviewRepository.save(itemReview);
        return itemReview.getId();
    }

    @Override
    public void modify(ReviewDto itemReviewDto) {
       Optional<Review> result = reviewRepository.findById(itemReviewDto.getReviewnum());
       if(result.isPresent()){
        Review itemReview = result.get();
        itemReview.changeGrade(itemReviewDto.getGrade());
        itemReview.changeText(itemReviewDto.getText());

        reviewRepository.save(itemReview);
       }
    }

    @Override
    public void remove(Long reviewnum) {
        reviewRepository.deleteById(reviewnum);
    }    

   // 추천 상태 토글 메서드
   @Override
    public String toggleRecommend(Long reviewnum, String email) {
            // 리뷰 조회
        Review review = reviewRepository.findById(reviewnum)
        .orElseThrow(() -> new IllegalArgumentException("Invalid review ID: " + reviewnum));

        // 이메일로 회원 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }

        boolean isRecommended = review.getRecommendedMembers().contains(member);
        if (isRecommended) {
            review.getRecommendedMembers().remove(member);
            review.decrementRCnt();
            reviewRepository.save(review);
            return "추천 취소";
        } else {
            review.getRecommendedMembers().add(member);
            review.incrementRCnt();
            reviewRepository.save(review);
            return "추천 완료";
        }
    }

    @Override
    public String validRegist(Long itemId, String email) {
        Long memberId = memberRepository.findByEmail(email).getId();
        Optional<ItemCode> itemCode =codeRepository.findByItemAndMember(itemId,memberId);
        if(itemCode.isPresent()){
            return "valid";
        }else{
            return "invalid";
        }
    }
}

