package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.kyc.ApproveKyc;
import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.service.knowYourCustomer.KnowYourCustomerService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KnowYourCustomerController {
    private final KnowYourCustomerService kycService;

    @PostMapping()
    @MessageMapping("/register")
    public ResponseEntity<?> registerKyc(@Valid  @RequestBody KycDto kyc) {
         return kycService.register(kyc);
    }

    @PutMapping("/{kycId}")
    public ResponseEntity<?> updateKyc(@Valid @RequestBody ApproveKyc kycDto, @PathVariable int kycId) throws MessagingException {
        return kycService.approveKyc(kycDto,kycId);
    }

    @GetMapping("/{kycId}")
    public ResponseEntity<?> getKycs(@PathVariable int kycId) {
        return kycService.getKycById(kycId);
    }

    @GetMapping()
    public ResponseEntity<?> getAllKycs(
                                        @RequestParam(value = "page",defaultValue = "0") Integer page,
                                        @RequestParam(value = "limit",defaultValue = "10") Integer size) {
        return kycService.getKycs(page,size);
    }
}
