package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import com.second_hand_auction_system.models.WithdrawRequest;
import com.second_hand_auction_system.service.withdrawRequest.IWithdrawRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/withdrawRequest")
@RequiredArgsConstructor
public class WithdrawRequestController {
    private final IWithdrawRequestService withdrawRequestService;

    @PostMapping("")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequestDTO withdrawRequest) {
        return  withdrawRequestService.requestWithdraw(withdrawRequest);
    }
}
