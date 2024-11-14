package com.playground.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.playground.dto.ReviewDto;
import com.playground.entity.Item;
import com.playground.entity.Member;
import com.playground.entity.Review;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

public interface ReviewService {

    //게임의 모든 게임리뷰를 가져온다.
    Page<ReviewDto> getListOfItem(Long id, Pageable pageable, String currentUserEmail);  // 페이징 적용

    //게임 리뷰를 추가
    Long register(ReviewDto itemReviewDto);

    //특정한 게임리뷰 수정
    void modify(ReviewDto itemReviewDto);

    //게임 리뷰 삭제
    void remove(Long reviewnum);

     //리뷰 추천
    //  void reviewRecommend(Long reviewnum); // 추천 증가 메서드
    String toggleRecommend(Long reviewnum, String email);

    // 게임 산 사람만 리뷰가능, 한게임당 한개만 리뷰작성 가능
    String validRegist(Long id, String email);

    List<ReviewDto> getReviewList(String email);

    default Review dtoToEntity(ReviewDto itemReviewDto){

        Review itemReview = Review.builder()
                .id(itemReviewDto.getReviewnum())
                .item(Item.builder().id(itemReviewDto.getId()).build())
                .member(Member.builder().id(itemReviewDto.getMember_id()).build())
                .grade(itemReviewDto.getGrade())
                .text(HtmlUtils.htmlEscape(itemReviewDto.getText()))
                .build();

        return itemReview;
    }

    default ReviewDto entityToDto(Review itemReview, String currentUserEmail){
        boolean isRecommended = false;
        if (currentUserEmail != null && itemReview.getRecommendedMembers().stream().anyMatch(m -> m.getEmail().equals(currentUserEmail))) {
            isRecommended = true;
        }

        ReviewDto itemReviewDto = ReviewDto.builder()
            .reviewnum(itemReview.getId())
            .member_id(itemReview.getMember().getId())
            .id(itemReview.getItem().getId())              
            .email(itemReview.getMember().getEmail())
            .grade(itemReview.getGrade())
            .text(itemReview.getText())
            .regTime(itemReview.getRegTime())
            .updateTime(itemReview.getUpdateTime())
            .rCnt(itemReview.getRCnt())
            .isRecommended(isRecommended) // 현재 사용자의 추천 여부 추가
            .profileImg(itemReview.getMember().getProfileImg())
            .build();

        return itemReviewDto;
    }
}
