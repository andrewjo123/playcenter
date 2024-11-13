package com.playground.controller;

import com.playground.dto.*;
import com.playground.service.DibsService;
import com.playground.service.ItemService;
import com.playground.service.MemberService;
import com.playground.service.ReviewService;
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

import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final DibsService dibsService;
    private final MemberService memberService;
    private final ReviewService reviewService;

    @GetMapping(value = {"/", "/main"})
    public String getItems(Model model, HttpServletRequest request) {
        // 모든 아이템 가져오기
        List<MainItemDto> all = itemService.getMainItem(null, null);

        // 회사별 아이템 가져오기
        List<MainItemDto> steam = itemService.getMainItem("steam", null);
        List<MainItemDto> nintendo = itemService.getMainItem("nintendo", null);
        List<MainItemDto> ps = itemService.getMainItem("ps", null);

        Map<String, List<MainItemDto>> result=new LinkedHashMap<>();
        result.put("all",all);
        result.put("steam",steam);
        result.put("nintendo",nintendo);
        result.put("ps",ps);

        model.addAttribute("result", result);
        /*창 정보 가져오기*/
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent.toLowerCase().contains("mobile");
        model.addAttribute("isMobile", isMobile);

        return "main";
    }

    @GetMapping(value = {"/list", "/steam", "/ps", "/nintendo"})
    public String defaultList(ItemSearchDto itemSearchDto, ItemCategoryDto itemCategoryDto, Optional<Integer> page, Model model, HttpServletRequest request, Principal principal) {
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
        model.addAttribute("challenge", memberService.getChallengeInfo(email));
        model.addAttribute("reviewList", reviewService.getReviewList(email));
        return "mypage/mypage";
    }

    @RequestMapping(value = {"/pointDuration"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<List<PointHistDto>> addList(@RequestParam("date") int date, Principal principal) {
        List<PointHistDto> pointList = memberService.getPointHistory(principal.getName(), date);
        return new ResponseEntity<>(pointList, HttpStatus.OK);
    }

    @RequestMapping(value={"/acceptChallenge"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> challenge(@RequestParam("challenge") String challenge, Principal principal){
        String result=memberService.validAndAcceptChallenge(challenge,principal.getName());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value={"/changeProfileImg"}, method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> changeProfileImg(@RequestParam("profileImg") String profileImg, Principal principal){
        String result=memberService.changeProfileImg(profileImg,principal.getName());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}