package com.playground.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import com.playground.dto.ReviewDto;
import com.playground.entity.Item;
import com.playground.entity.Review;
import com.playground.repository.MemberRepository;
import com.playground.repository.ReviewRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<ReviewDto> getListOfItem(Long id) {
        Item item = Item.builder().id(id).build();
        System.out.println(item);
        List<Review> result = reviewRepository.findByItem(item);
        System.out.println(result);
        return result.stream().map(itemReview->entityToDto(itemReview)).collect(Collectors.toList());
        
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


}

