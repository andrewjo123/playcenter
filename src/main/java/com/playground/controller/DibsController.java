package com.playground.controller;

import com.playground.dto.CartDetailDto;
import com.playground.dto.DibsDto;
import com.playground.service.DibsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DibsController {

    private final DibsService dibsService;

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
    public String dibsHist(Principal principal, Model model){

        if (principal == null) {
            return "redirect:/members/login";
        }

        List<DibsDto> dibsDetailList = dibsService.getDibsList(principal.getName());
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

}