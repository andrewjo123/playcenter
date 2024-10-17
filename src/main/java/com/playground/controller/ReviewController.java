package com.playground.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.playground.dto.ReviewDto;
import com.playground.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Log4j2
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{id}/all")
    public ResponseEntity<List<ReviewDto>> getList(@PathVariable("id") Long id){
        log.info("--------------list---------------");
        log.info("id: " + id);

        List<ReviewDto> reviewDTOList = reviewService.getListOfItem(id);

        return new ResponseEntity<>(reviewDTOList, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Long> addReview(@RequestBody ReviewDto itemReviewDto){
        log.info("--------------add Review---------------");
<<<<<<< HEAD
        log.info("ReviewDto: " + itemReviewDto);
=======
        log.info("ReviewDto: " + itemReviewDto.getId());
>>>>>>> 신영
        System.out.println("###################################");

        Long reviewnum = reviewService.register(itemReviewDto);

        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }

    @PutMapping("/{id}/{reviewnum}")
    public ResponseEntity<Long> modifyReview(@PathVariable Long reviewnum,
                                             @RequestBody ReviewDto itemReviewDto){
        log.info("---------------modify MovieReview--------------" + reviewnum);
        log.info("ReviewDto: " + itemReviewDto);

        reviewService.modify(itemReviewDto);

        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/{reviewnum}")
    public ResponseEntity<Long> removeReview( @PathVariable Long reviewnum){
        log.info("---------------modify removeReview--------------");
        log.info("reviewnum: " + reviewnum);

        reviewService.remove(reviewnum);

        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }

}

