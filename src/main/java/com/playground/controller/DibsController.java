package com.playground.controller;

import com.playground.dto.CartItemDto;
import com.playground.dto.DibsDto;
import com.playground.service.CartService;
import com.playground.service.DibsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DibsController {

    private final DibsService dibsService;
    private final CartService cartService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/dibsAdd")
    public @ResponseBody ResponseEntity addDibs(@RequestBody DibsDto dibsDto, Principal principal){

        String email = principal.getName();
        Long dibsItemId;

        try {
            dibsItemId = dibsService.addDibs(dibsDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(dibsItemId, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/dibs")
    public String dibsHist(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/members/login";
        }

        // 로그인된 사용자의 Dibs 목록을 가져옵니다.
        List<DibsDto> dibsDetailList = dibsService.getDibsList(principal.getName());

        // DibsDto 객체에 품절 상태를 확인하여 모델에 추가
        for (DibsDto dibsItem : dibsDetailList) {
            // 품절 상태를 갱신할 필요는 없지만, 추가 처리가 필요하다면 여기에 작성
            // 예: dibsItem.setSoldOut(dibsItem.isSoldOut());
        }

        // 품절 상태를 모델에 추가하여 품절 배지가 Thymeleaf 템플릿에서 사용 가능
        model.addAttribute("dibsItems", dibsDetailList);
        return "dibs/dibsList";
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping(value = "/dibsDelete")
    public @ResponseBody ResponseEntity deleteDibsItem(@RequestBody DibsDto dibsDto, Principal principal){

        if (principal == null) {
            return new ResponseEntity<>("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }
        Long deletedId= 0L;
        if(dibsDto.getDibsItemId()!=null){
            deletedId=dibsService.deleteDibsItemFromId(dibsDto.getDibsItemId());
        }else{
            deletedId=dibsService.deleteDibsItem(dibsDto.getItemId(), principal.getName());
        }

        return new ResponseEntity<Long>(deletedId, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @RequestMapping(value="/dibsCount",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Integer> dibsCount(@RequestParam("email") String email){
        int result=dibsService.getDibsCount(email);
        // ResponseEntity로 반환
        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/dibsToCart")
    public @ResponseBody ResponseEntity dibsToCart(@RequestParam("dibsItemId")Long dibsItemId, Principal principal){

        CartItemDto cartItemDto=new CartItemDto();
        cartItemDto.setItemId(dibsService.getDibsItem(dibsItemId));
        cartItemDto.setCount(1);
        Long cartItemId;
        try {
            cartItemId = cartService.addCart(cartItemDto, principal.getName());
            if(cartItemId!=0L){
                dibsService.deleteDibsItemFromId(dibsItemId);
            }
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(cartItemId, HttpStatus.OK);
    }

}