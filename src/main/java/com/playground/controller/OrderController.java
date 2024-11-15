package com.playground.controller;

import com.playground.dto.OrderDto;
import com.playground.dto.OrderHistDto;
import com.playground.service.CartService;
import com.playground.service.OrderService;
import com.playground.service.RefundService;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final RefundService refundService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/order")
    public @ResponseBody ResponseEntity order(@RequestBody @Valid OrderDto orderDto
            , BindingResult bindingResult, Principal principal){

        if(bindingResult.hasErrors()){
            StringBuilder sb = new StringBuilder();
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();

            for (FieldError fieldError : fieldErrors) {
                sb.append(fieldError.getDefaultMessage());
            }

            return new ResponseEntity<String>(sb.toString(), HttpStatus.BAD_REQUEST);
        }

        String email = principal.getName();
        Long orderId;

        try {
            orderId = orderService.order(orderDto, email);
        } catch(Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = {"/orders", "/orders/{page}"})
    public String orderHist(@PathVariable("page") Optional<Integer> page, @RequestParam("date") Optional<Integer> date, Principal principal, Model model){

        Pageable pageable = PageRequest.of(page.isPresent() ? page.get() : 0, 8);
        Page<OrderHistDto> ordersHistDtoList = orderService.getOrderList(principal.getName(),date.isPresent()?date.get():3,pageable);

        model.addAttribute("orders", ordersHistDtoList);
        model.addAttribute("page", pageable.getPageNumber());
        model.addAttribute("maxPage", ordersHistDtoList.getTotalPages());
        model.addAttribute("date", date.isPresent()?date.get():3);

        return "order/orderHist";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/order/{orderId}/cancel")
    public @ResponseBody ResponseEntity cancelOrder(@PathVariable("orderId") Long orderId , Principal principal) throws IOException {

        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("주문 취소 권한이 없습니다.", HttpStatus.FORBIDDEN);
        }

        orderService.cancelOrder(orderId);
        String token=refundService.getToken(restApiKey,restApiSecret);
        refundService.refundWithToken(token,orderId);
        return new ResponseEntity<Long>(orderId, HttpStatus.OK);
    }

    // 추가
    // 결제창
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/order/payment")
    public String goPayment(@RequestParam("orderId") Long orderId, @RequestParam Map<String, String> params, Principal principal, Model model, HttpServletRequest request){
        if(!orderService.validateOrder(orderId, principal.getName())){
            return "member/memberLoginForm";
        }
        List<OrderHistDto> orders = orderService.getPayList(orderId);
        String[] members=orderService.findBuyer(orderId);
        model.addAttribute("orders", orders);
        model.addAttribute("buyer",members[0]);
        model.addAttribute("point",members[1]);
        model.addAttribute("email",principal.getName());
        model.addAttribute("cartId",params);
        /*창 정보 가져오기*/
        String userAgent = request.getHeader("User-Agent");
        boolean isMobile = userAgent.toLowerCase().contains("mobile");
        model.addAttribute("isMobile", isMobile);
        return "order/payment";

    }

    @Value("${imp.api.key}")
    private String restApiKey;

    @Value("${imp.api.secretkey}")
    private String restApiSecret;

    private final IamportClient iamportClient;

    @Autowired
    public OrderController(OrderService orderService, CartService cartService, RefundService refundService, @Value("${imp.api.key}") String restApiKey,
                           @Value("${imp.api.secretkey}") String restApiSecret) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.refundService=refundService;
        this.restApiKey = restApiKey;
        this.restApiSecret = restApiSecret;
        this.iamportClient = new IamportClient(restApiKey, restApiSecret);
    }

    //iamport 서버에서 결제 1차검증
    @RequestMapping(value="/order/validPay/{imp_uid}",method = {RequestMethod.POST})
    @ResponseBody
    public IamportResponse<Payment> vaildatePay(@PathVariable("imp_uid") String imp_uid)
            throws IamportResponseException, IOException {
        return iamportClient.paymentByImpUid(imp_uid);
    }
    // DB에서 결제 2차검증
    @RequestMapping(value="/order/validDB",method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> validateWithDB(@RequestParam("orderId")Long orderId, @RequestParam("totalPrice")Long totalPrice, @RequestParam("usePoint")Long usePoint ,@RequestParam Map<String, String> params,
                                                 Principal principal) throws IOException {

        String result=orderService.validpay(orderId,totalPrice,usePoint);
        if(result.equals("ok")){
            orderService.payedOrder(orderId, usePoint);
            params.remove("orderId");
            params.remove("totalPrice");
            params.remove("usePoint");
            if (!params.isEmpty()) {
                params.forEach((key, value) -> {
                    cartService.deleteCartItem(Long.valueOf(value));
                });
            }
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else{
            String token=refundService.getToken(restApiKey,restApiSecret);
            refundService.refundWithToken(token,orderId);
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    // 결제 전 수량체크
    @RequestMapping(value="/order/checkStack",method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<List<String>> stack(@RequestParam Map<String, String> params){
        Long orderId=Long.parseLong(params.get("orderId"));
        params.remove("orderId");
        List<String> result=new ArrayList<>();
        if(params.isEmpty()){
            result=orderService.checkStackById(orderId);
        }else{
            result=orderService.checkStack(params);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //취소누르면 order삭제
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/order/removeOrder")
    public String removeOrderList(@RequestParam("orderId")Long orderId){
        orderService.removeList(orderId);
        return "redirect:/";
    }

    // 2차 검증에서 error 날 경우 환불
    @RequestMapping(value="/order/autoCancel",method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<String> validateWithDB(@RequestParam("orderId")Long orderId) throws IOException {
        String token=refundService.getToken(restApiKey,restApiSecret);
        refundService.refundWithToken(token,orderId);
        return new ResponseEntity<>("refund", HttpStatus.OK);
    }

    // 1023 1730 추가
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/order/sendCode")
    @ResponseBody
    public ResponseEntity<String> sendCodes(@RequestParam("orderId") Long orderId , Principal principal) throws IOException {

        if(!orderService.validateOrder(orderId, principal.getName())){
            return new ResponseEntity<String>("코드발송권한이 없습니다.", HttpStatus.FORBIDDEN);
        }
        String email=principal.getName();
        String result=orderService.sendAllCodes(orderId,email);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // item detail에서 바로 결제할 때 수량체크
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/itemDetailToOrder")
    public @ResponseBody ResponseEntity<Long> itemDetailToOrder(@RequestParam("itemId")Long itemId, @RequestParam("count")int count,Principal principal){
        Long result=orderService.checkStackFromItemDtl(itemId,count,principal.getName());
        return new ResponseEntity<Long>(result, HttpStatus.OK);
    }

}