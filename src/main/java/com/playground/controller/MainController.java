package com.playground.controller;

import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.service.ItemService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;

    @GetMapping(value = "/")
    public String main(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model){
        System.out.println(itemSearchDto.getSearchQuery());
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, pageable);

        model.addAttribute("items", items);
        model.addAttribute("itemSearchDto", itemSearchDto);
        model.addAttribute("maxPage", 5);

        return "main";
    }

    @RequestMapping(value="/steam",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> steam(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model){

        System.out.println(itemSearchDto.getSearchQuery());
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<MainItemDto> items = itemService.getMainItemPage2("steam", itemSearchDto, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("itemSearchDto", itemSearchDto);
        response.put("maxPage", 5);

        // ResponseEntity로 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/ps",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ps(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<MainItemDto> items = itemService.getMainItemPage2("ps", itemSearchDto, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("itemSearchDto", itemSearchDto);
        response.put("maxPage", 5);

        // ResponseEntity로 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/nintendo",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> nintendo(ItemSearchDto itemSearchDto, Optional<Integer> page, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        Page<MainItemDto> items = itemService.getMainItemPage2("nintendo", itemSearchDto, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("items", items);
        response.put("itemSearchDto", itemSearchDto);
        response.put("maxPage", 5);

        // ResponseEntity로 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}