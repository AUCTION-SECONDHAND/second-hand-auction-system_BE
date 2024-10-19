package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.kyc.ApproveKyc;
import com.second_hand_auction_system.dtos.request.kyc.KycDto;
import com.second_hand_auction_system.service.knowYourCustomer.KnowYourCustomerService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KnowYourCustomerController {
    private final KnowYourCustomerService kycService;

    @PostMapping()
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
    public ResponseEntity<?> getAllKycs(@RequestParam(required = false) String search,
                                        @RequestParam(required = false) Integer page,
                                        @RequestParam(required = false) Integer size) {
        return kycService.getKycs(search,page,size);
    }
}
