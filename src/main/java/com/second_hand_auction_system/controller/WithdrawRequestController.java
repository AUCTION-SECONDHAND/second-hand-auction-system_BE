package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawApprove;
import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.service.VNPay.VNPAYService;
import com.second_hand_auction_system.service.withdrawRequest.IWithdrawRequestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/withdrawRequest")
@RequiredArgsConstructor
public class WithdrawRequestController {
    private final IWithdrawRequestService withdrawRequestService;
    private final VNPAYService vnpayService;

    @PostMapping("")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDTO withdrawRequest) {
        return  withdrawRequestService.requestWithdraw(withdrawRequest);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> approveRequest(@PathVariable("id") Integer id, @RequestBody WithdrawApprove withdrawApprove, HttpServletRequest request) {
        return withdrawRequestService.approve(id,withdrawApprove,request);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> submitOrder(@RequestParam("amount") int orderTotal,@RequestParam("withdrawInfo") int withdraw, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        return vnpayService.createOrder(orderTotal, withdraw, baseUrl);
    }

    @GetMapping("/vnpay-payment")
    public String GetMapping(HttpServletRequest request, Model model){
        int paymentStatus =vnpayService.orderReturn(request);

        String orderInfo = request.getParameter("vnp_OrderInfo");
        String paymentTime = request.getParameter("vnp_PayDate");
        String transactionId = request.getParameter("vnp_TransactionNo");
        String totalPrice = request.getParameter("vnp_Amount");

        model.addAttribute("orderId", orderInfo);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentTime", paymentTime);
        model.addAttribute("transactionId", transactionId);

        return paymentStatus == 1 ? "ordersuccess" : "orderfail";
    }
}
