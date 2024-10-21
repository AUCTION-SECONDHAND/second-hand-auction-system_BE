package com.second_hand_auction_system.service.VNPay;

import org.springframework.http.ResponseEntity;

public interface VNPaySerivce {
    ResponseEntity<?> createOrder(int orderTotal,  String baseUrl);
}
