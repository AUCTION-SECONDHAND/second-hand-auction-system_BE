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
import org.springframework.web.multipart.MultipartFile;

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
    public ResponseEntity<?> approve(@Valid @RequestBody ApproveKyc kycDto, @PathVariable int kycId) throws MessagingException {
        return kycService.approveKyc(kycDto,kycId);
    }

    @PutMapping("/update-profileKyc")
    public ResponseEntity<?> updateKyc (@Valid @RequestBody KycDto kycDto) throws MessagingException {
        return kycService.updateKyc(kycDto);
    }

    @GetMapping("/{kycId}")
    public ResponseEntity<?> getKyc(@PathVariable int kycId) {
        return kycService.getKycById(kycId);
    }

    @GetMapping("user")
    public ResponseEntity<?> getUserKyc() {
        return kycService.getKycUserById();
    }

    @GetMapping()
    public ResponseEntity<?> getAllKyc(
                                        @RequestParam(value = "page",defaultValue = "0") Integer page,
                                        @RequestParam(value = "limit",defaultValue = "10") Integer size) {
        return kycService.getKycs(page,size);
    }
}
