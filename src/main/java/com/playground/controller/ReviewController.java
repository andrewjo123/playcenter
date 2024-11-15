package com.playground.controller;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.playground.dto.ReviewDto;
import com.playground.service.ReviewService;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 리스트 가져오기 (페이징)
    @GetMapping("/{id}/all")
    public ResponseEntity<Page<ReviewDto>> getList(
          @PathVariable("id") Long id,
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "5") int size,
          @RequestParam(defaultValue = "regTime") String sort,
          Principal principal) {
      String currentUserEmail = (principal != null) ? principal.getName() : null; //로그인 안한 사용자는 댓글 볼 수 있음
      
      // 페이지 요청 객체 생성
      Pageable pageable = PageRequest.of(page, size,
      sort.equals("rating") ? Sort.by(Sort.Direction.DESC, "grade") : 
      sort.equals("asc") ? Sort.by(Sort.Direction.ASC, "grade") : 
      sort.equals("recommendation") ? Sort.by(Sort.Direction.DESC, "rCnt") : 
      Sort.by(Sort.Direction.DESC, "regTime"));
      
      // 리뷰 목록을 서비스에서 받아옴
      Page<ReviewDto> reviewDTOPage = reviewService.getListOfItem(id, pageable, currentUserEmail);

      return new ResponseEntity<>(reviewDTOPage, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}")
    public ResponseEntity<Long> addReview(@RequestBody ReviewDto itemReviewDto){
        Long reviewnum = reviewService.register(itemReviewDto);
        
        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }
    
    // 리뷰 추천 토글
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{reviewnum}/recommend")
    public ResponseEntity<String> toggleRecommend(
        @PathVariable Long reviewnum, 
        @RequestParam("email") String email) {

        String result = reviewService.toggleRecommend(reviewnum, email);
        
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
  
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{id}/{reviewnum}")
    public ResponseEntity<Long> modifyReview(@PathVariable Long reviewnum,
                                             @RequestBody ReviewDto itemReviewDto){
        reviewService.modify(itemReviewDto);

        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/{reviewnum}")
    public ResponseEntity<Long> removeReview( @PathVariable Long reviewnum){

        reviewService.remove(reviewnum);

        return new ResponseEntity<>( reviewnum, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value = "/validRegist", method = {RequestMethod.POST})
    public ResponseEntity<String> validRegist(@RequestParam("itemId")Long itmeId, Principal principal){
        String email = principal.getName();
        String result = reviewService.validRegist(itmeId, email);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/deleteFromMyPage")
    public ResponseEntity<List<ReviewDto>> removeReviewFromMyPage(@RequestParam Long reviewNum, Principal principal){
        reviewService.remove(reviewNum);
        return new ResponseEntity<>(reviewService.getReviewList(principal.getName()), HttpStatus.OK);
    }




}

