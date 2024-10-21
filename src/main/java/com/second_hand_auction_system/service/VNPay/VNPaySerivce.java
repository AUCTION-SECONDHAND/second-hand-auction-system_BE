package com.second_hand_auction_system.service.VNPay;

import com.second_hand_auction_system.dtos.request.withdrawRequest.WithdrawRequestDTO;
import org.springframework.http.ResponseEntity;

public interface VNPaySerivce {
    ResponseEntity<?> createOrder(int orderTotal, int withdrawId,  String baseUrl);
}
