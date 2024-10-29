package com.playground.controller;

import com.playground.dto.ItemCategoryDto;
import com.playground.dto.ItemSearchDto;
import com.playground.dto.MainItemDto;
import com.playground.service.ItemService;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.servlet.http.HttpServletRequest;
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

    @GetMapping(value={"/","/main"})
    public String mains(Model model){

        return "main";
    }

    @GetMapping(value ={"/list","/steam","/ps","/nintendo"})
    public String defaultList(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto,Optional<Integer> page, Model model, HttpServletRequest request){
        System.out.println(itemCategoryDto.isAction());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        String company = request.getRequestURI().substring(1);
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        if(company.equals("list")){
            Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, itemCategoryDto,pageable);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("maxPage", items.getTotalPages());
            model.addAttribute("company","all");
            model.addAttribute("tag",itemCategoryDto);
        } else {
            Page<MainItemDto> items = itemService.getMainItemPage2(company, itemSearchDto, itemCategoryDto,pageable);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("maxPage", items.getTotalPages());
            model.addAttribute("company",company);
            model.addAttribute("tag",itemCategoryDto);
        }

        return "item/itemList";
    }

    @RequestMapping(value= {"/steamMore", "/psMore", "/nintendoMore", "/allMore"},method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addList(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Optional<Integer> page, HttpServletRequest request){

        String company = request.getRequestURI().substring(1);
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : itemSearchDto.getNthPage(), 6);

        Map<String, Object> response = new HashMap<>();
        if(company.equals("allMore")){
            Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, itemCategoryDto,pageable);
            response.put("items", items);
            response.put("itemSearchDto", itemSearchDto);
            response.put("maxPage", items.getTotalPages());
        } else {
            String companies="";
            if (company.length() > 4) {
                companies = company.substring(0, company.length() - 4);
            }
            Page<MainItemDto> items = itemService.getMainItemPage2(companies, itemSearchDto, itemCategoryDto,pageable);
            response.put("items", items);
            response.put("itemSearchDto", itemSearchDto);
            response.put("maxPage", items.getTotalPages());
        }

        // ResponseEntity로 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value="/support", method = RequestMethod.GET)
    public String support() {
        return "support/supportmain";
    }

}