package com.playground.controller;

import com.playground.dto.*;
import com.playground.service.DibsService;
import com.playground.service.ItemService;
import com.playground.service.MemberService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final DibsService dibsService;
    private final MemberService memberService;

    @GetMapping(value = {"/", "/main"})
    public String getItems(Model model) {
        Pageable pageable = PageRequest.of(0, 3);

        // 모든 아이템 가져오기
        Page<MainItemDto> allItems = itemService.getMainItem(null, null, null, pageable);

        // 회사별 아이템 가져오기
        Page<MainItemDto> steamItems = itemService.getMainItem("steam", null, null, pageable);
        Page<MainItemDto> nintendoItems = itemService.getMainItem("nintendo", null, null, pageable);
        Page<MainItemDto> psItems = itemService.getMainItem("ps", null, null, pageable);

        model.addAttribute("allItems", allItems);
        model.addAttribute("steamItems", steamItems);
        model.addAttribute("nintendoItems", nintendoItems);
        model.addAttribute("psItems", psItems);

        return "main";
    }

    @GetMapping(value = {"/list", "/steam", "/ps", "/nintendo"})
    public String defaultList(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Optional<Integer> page, Model model, HttpServletRequest request, Principal principal) {
        System.out.println(itemCategoryDto.isAction());
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        String company = request.getRequestURI().substring(1);
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 6);
        List<Long> dibsList = new ArrayList<>();
        if (principal != null) {
            dibsList = dibsService.dibsListforItemId(principal.getName());
        } else {
            dibsList.add(0L);
        }
        model.addAttribute("dibsList", dibsList);
        if (company.equals("list")) {
            Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, itemCategoryDto, pageable);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("maxPage", items.getTotalPages());
            model.addAttribute("company", "all");
            model.addAttribute("tag", itemCategoryDto);
        } else {
            Page<MainItemDto> items = itemService.getMainItemPage2(company, itemSearchDto, itemCategoryDto, pageable);
            model.addAttribute("items", items);
            model.addAttribute("itemSearchDto", itemSearchDto);
            model.addAttribute("maxPage", items.getTotalPages());
            model.addAttribute("company", company);
            model.addAttribute("tag", itemCategoryDto);
        }

        return "item/itemList";
    }

    @RequestMapping(value = {"/steamMore", "/psMore", "/nintendoMore", "/allMore"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addList(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Optional<Integer> page, HttpServletRequest request, Principal principal) {

        String company = request.getRequestURI().substring(1);
        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : itemSearchDto.getNthPage(), 6);
        List<Long> dibsList = new ArrayList<>();
        if (principal != null) {
            dibsList = dibsService.dibsListforItemId(principal.getName());
        } else {
            dibsList.add(0L);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("dibsList", dibsList);
        if (company.equals("allMore")) {
            Page<MainItemDto> items = itemService.getMainItemPage(itemSearchDto, itemCategoryDto, pageable);
            response.put("items", items);
            response.put("itemSearchDto", itemSearchDto);
            response.put("maxPage", items.getTotalPages());
        } else {
            String companies = "";
            if (company.length() > 4) {
                companies = company.substring(0, company.length() - 4);
            }
            Page<MainItemDto> items = itemService.getMainItemPage2(companies, itemSearchDto, itemCategoryDto, pageable);
            response.put("items", items);
            response.put("itemSearchDto", itemSearchDto);
            response.put("maxPage", items.getTotalPages());
        }

        // ResponseEntity로 반환
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = "/support", method = RequestMethod.GET)
    public String support() {
        return "support/supportmain";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/mypage")
    public String mypage(Model model, Principal principal) {
        String email = principal.getName();
        MemberFormDto userDto = memberService.getUser(email);
        model.addAttribute("memberForm", userDto);
        int date = 6;
        model.addAttribute("pointList", memberService.getPointHistory(principal.getName(), date));
        return "mypage/mypage";
    }

    @RequestMapping(value = {"/pointDuration"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<List<PointHistDto>> addList(@RequestParam("date") int date, Principal principal) {
        List<PointHistDto> pointList = memberService.getPointHistory(principal.getName(), date);
        return new ResponseEntity<>(pointList, HttpStatus.OK);
    }
}