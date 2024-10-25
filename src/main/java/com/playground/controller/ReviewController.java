package com.playground.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.playground.dto.ReviewDto;
import com.playground.service.ReviewService;

@RestController
@RequestMapping("/reviews")
@Log4j2
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 리스트 가져오기 (페이징)
  @GetMapping("/{id}/all")
  public ResponseEntity<Page<ReviewDto>> getList(
          @PathVariable("id") Long id,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "5") int size,
          @RequestParam(defaultValue = "regTime") String sort) {
      
      log.info("리뷰 리스트 요청 - id: {}, page: {}, size: {}", id, page, size, sort);

      // 페이지 요청 객체 생성
      Pageable pageable = PageRequest.of(page, size,
      sort.equals("rating") ? Sort.by(Sort.Direction.DESC, "grade") : 
      sort.equals("asc") ? Sort.by(Sort.Direction.ASC, "grade") : 
      Sort.by(Sort.Direction.DESC, "regTime"));
      
      // 리뷰 목록을 서비스에서 받아옴
      Page<ReviewDto> reviewDTOPage = reviewService.getListOfItem(id, pageable);

      return new ResponseEntity<>(reviewDTOPage, HttpStatus.OK);
  }

    // @GetMapping("/{id}/all")
    // public ResponseEntity<List<ReviewDto>> getList(@PathVariable("id") Long id){
    //     log.info("--------------list---------------");
    //     log.info("id: " + id);

    //     List<ReviewDto> reviewDTOList = reviewService.getListOfItem(id);

    //     return new ResponseEntity<>(reviewDTOList, HttpStatus.OK);
    // }

    @PostMapping("/{id}")
    public ResponseEntity<Long> addReview(@RequestBody ReviewDto itemReviewDto){
        log.info("--------------add Review---------------");
        log.info("ReviewDto: " + itemReviewDto.getId());
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

