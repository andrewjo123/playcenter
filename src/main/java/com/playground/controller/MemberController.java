package com.playground.controller;

import com.playground.dto.MemberDetailDto;
import com.playground.dto.MemberFormDto;
import com.playground.dto.MemberSearchDto;
import com.playground.entity.Member;
import com.playground.service.EmailService;
import com.playground.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@RequestMapping("/members")
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @GetMapping(value = "/new")
    public String memberForm(Model model){
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping(value = "/new")
    public String newMember(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model){

        if(bindingResult.hasErrors()){
            return "member/memberForm";
        }

        try {
            Member member = Member.createMember(memberFormDto, passwordEncoder);

            memberService.saveMember(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String loginMember(HttpServletRequest request) {
        // 현재 인증 정보를 가져옵니다.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 로그인 상태 확인 (인증 정보 존재 확인, 사용자가 인증된 상태인지 확인, 인증되지않은 사용자가 아니라는 것을 확인
        //AnonymousAuthenticationToken: 인증되지 않은 사용자를 나타내기 위해 사용
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 이전 페이지로 리다이렉션
//            String previousPage = request.getHeader("Referer");
//            System.out.println("asdasdasdnsdcbvfskjdbvjskdnbvjks"+previousPage);
            return "redirect:/"; // 이전 페이지로 리다이렉션
        }

        // 로그인하지 않은 사용자라면 로그인 폼을 보여줍니다.
        return "member/memberLoginForm";
    }

    @GetMapping(value = "/login/error")
    public String loginError(Model model,HttpServletRequest request){
        String site=(String) request.getSession().getAttribute("resign");
        if(site==null|| site.isEmpty()){
            model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        } else {
            request.getSession().removeAttribute("resign");
            model.addAttribute("loginErrorMsg", "회원 탈퇴 신청된 계정입니다. 복구를 원하시면 관리자에게 문의하세요.");
        }
        return "member/memberLoginForm";
    }
    // 정관수 추가
    @GetMapping(value="/modify")
    public String modUser(Model model, Principal principal){
        String userid = principal.getName();
        MemberFormDto memberFormDto=memberService.getUser(userid);
        model.addAttribute("memberFormDto", memberFormDto);
        return "member/memberModify";
    }

    @PostMapping(value="/modify")
    public String modifyUser(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Principal principal, Model model){
        if(principal==null){
            return "/";
        }
        if(bindingResult.hasErrors()){
            model.addAttribute("memberFormDto", memberFormDto);
            return "member/memberModify";
        }
        memberFormDto.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        memberService.updateMemberAll(memberFormDto);
        return "redirect:/members/logout";
    }

    //소셜 로그인 전용 수정페이지
    @GetMapping(value="/modify2")
    public String modUser2(Model model, HttpServletRequest request){
        String email = (String) request.getSession().getAttribute("email");
        model.addAttribute("email", email);
        request.getSession().removeAttribute("email");
        return "member/memberModifyAuth";
    }
    //소셜 로그인 전용 수정페이지
    @PostMapping(value="/modify2")
    public String modifyUser2(MemberFormDto memberFormDto, Model model){
        memberFormDto.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        memberService.registSocialMember(memberFormDto);
        return "member/memberLoginForm";
    }

    //로그인폼 인증버튼클릭
    @RequestMapping(value="/checkEmail",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> checkEmail(@RequestParam("email")String email){
        String result=memberService.validateEmail(email);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //팝업창
    @GetMapping(value="/authEmail")
    public String emailPopup(@RequestParam("email")String email, @RequestParam("result")String result, Model model){
        model.addAttribute("email",email);
        model.addAttribute("result",result);
        return "member/authEmail";
    }

    // 이메일 코드 발송
    @RequestMapping(value="/sendCode",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> sendCode(@RequestParam("email")String email){
        String subject="[놀이마당]이메일 인증코드 발송";
        String code=randomMix(10);
        System.out.println(code);
        String body="인증코드는 "+code+" 입니다.";
        emailService.sendEmail(email, subject, body);
        memberService.setCode(email,code);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    // 코드 일치 확인
    @RequestMapping(value="/authCode",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> authCode(@RequestParam("email")String email,@RequestParam("code")String code){

        String result=memberService.compareCode(email,code);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //이메일 사용 확인(코드, 이메일 삭제)
    @RequestMapping(value="/authDelete",method = {RequestMethod.DELETE})
    @ResponseBody
    public ResponseEntity<String> authDelete(@RequestParam("email")String email){

        memberService.authDelete(email);
        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    //아이디 찾기
    @GetMapping("/findEmail")
    public String findEmail(){
        return "member/findEmail";
    }

    @RequestMapping(value="/getEmail",method = {RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<List<String>> findEmail(MemberFormDto memberFormDto){

        List<String> result=memberService.findEmail(memberFormDto.getName(), memberFormDto.getPhone());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 비밀번호 찾기
    @GetMapping("/findPw")
    public String findPw(Model model){
        return "member/findPw";
    }

    @PostMapping("/findPw")
    @Transactional
    public String sendEmail(MemberFormDto memberFormDto, RedirectAttributes attr) {
        System.out.println(memberFormDto.getEmail());
        String result=memberService.validBeforeSendPwd(memberFormDto.getEmail(), memberFormDto.getName(), memberFormDto.getPhone());
        System.out.println(result);
        if(result.equals("noEmail")||result.equals("notValid")){
            attr.addFlashAttribute("result",result);
            return "redirect:/members/findPw";
        }
        String subject="[놀이마당]임시 비밀번호 전송";
        String newPw=randomMix(12);
        String body="비밀번호가 "+newPw+"로 변경되었습니다. \n\n안전한 사용을 위해 로그인 후 가급적 빠르게 비밀번호를 변경해 주시길 바랍니다.";

        memberService.updateMember(memberFormDto.getEmail(), passwordEncoder.encode(newPw));
        emailService.sendEmail(memberFormDto.getEmail(), subject, body);
        return "member/memberLoginForm";
    }

    // 난수 생성 코드
    public static String randomMix(int range) {
        StringBuilder sb = new StringBuilder();
        Random rd = new Random();

        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < range; i++) {
            int index = rd.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    @GetMapping("/confirm")
    public String confirmLogin(Model model, HttpServletRequest request) {
         // confirm.html로 뷰 설정
        String email = (String) request.getSession().getAttribute("email");
        String site=(String) request.getSession().getAttribute("site");
        model.addAttribute("email", email);
        model.addAttribute("site",site);
        request.getSession().removeAttribute("email");
        request.getSession().removeAttribute("site");
        return "member/confirm";
    }

    @PostMapping("/confirm")
    public String processConfirmation(@RequestParam String email, @RequestParam String site, @RequestParam boolean approve) {
        if (approve) {
            // 사용자가 승인한 경우, 비소셜 계정을 소셜 계정으로 업데이트
            memberService.changeSocial(email);
            // 로그인 처리 계속
            return "redirect:/oauth2/authorization/"+site; // 로그인 성공 후 리다이렉트
        } else {
            // 사용자가 거부한 경우
            return "redirect:/members/login/error"; // 로그인 실패로 리다이렉트
        }
    }

    // 회원탈퇴
    @RequestMapping(value="/resign",method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> goResign(MemberFormDto dto){

        String result=memberService.valideResign(dto);
        memberService.changeResign(dto.getEmail());

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    // 정관수 끝
    // 조민 추가

    @GetMapping(value = {"/manageMember", "/manageMember/{page}"})
    public String memberManage(MemberSearchDto memberSearchDto, @PathVariable("page") Optional<Integer> page, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 3);  // 페이지네이션 설정
        Page<Member> members = memberService.getAdminMemberPage(memberSearchDto, pageable);

        model.addAttribute("members", members);  // 회원 리스트 전달
        model.addAttribute("memberSearchDto", memberSearchDto);  // 검색 필드 전달
        model.addAttribute("maxPage", 5);  // 최대 페이지 수 설정

        return "member/memberMng";
    }
    //조민끝
}

