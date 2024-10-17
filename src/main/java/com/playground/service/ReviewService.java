package com.playground.service;

import java.util.List;

import com.playground.dto.ReviewDto;
import com.playground.entity.Item;
import com.playground.entity.Member;
import com.playground.entity.Review;

public interface ReviewService {

    //영화의 모든 영화리뷰를 가져온다.
    List<ReviewDto> getListOfItem(Long id);

    //영화 리뷰를 추가
    Long register(ReviewDto itemReviewDto);

    //특정한 영화리뷰 수정
    void modify(ReviewDto itemReviewDto);

    //영화 리뷰 삭제
    void remove(Long reviewnum);

    default Review dtoToEntity(ReviewDto itemReviewDto){

        Review itemReview = Review.builder()
                .id(itemReviewDto.getReviewnum())
<<<<<<< HEAD
                .item(Item.builder().id(itemReviewDto.getMember_id()).build())
=======
                .item(Item.builder().id(itemReviewDto.getId()).build())
>>>>>>> 신영
                .member(Member.builder().id(itemReviewDto.getMember_id()).build())
                .grade(itemReviewDto.getGrade())
                .text(itemReviewDto.getText())
                .build();

        return itemReview;
    }

    default ReviewDto entityToDto(Review itemReview){

        ReviewDto itemReviewDto = ReviewDto.builder()
                .reviewnum(itemReview.getId())
<<<<<<< HEAD
                .id(itemReview.getItem().getId())
                .id(itemReview.getMember().getId())                
=======
                .member_id(itemReview.getMember().getId())
                .id(itemReview.getItem().getId())              
>>>>>>> 신영
                .email(itemReview.getMember().getEmail())
                .grade(itemReview.getGrade())
                .text(itemReview.getText())
                .regTime(itemReview.getRegTime())
                .updateTime(itemReview.getUpdateTime())
                .build();

        return itemReviewDto;
    }
}
