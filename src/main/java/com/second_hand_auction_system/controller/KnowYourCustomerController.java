package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.service.knowYourCustomer.KnowYourCustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KnowYourCustomerController {
    private final KnowYourCustomerService kycService;

    @PostMapping()
    public ResponseEntity<?> registerKyc(@Valid  @RequestBody KycDto kyc) {
        return kycService.register(kyc);
    }
}
