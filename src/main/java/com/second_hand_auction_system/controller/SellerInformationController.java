package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.sellerInfomation.SellerInformationDto;
import com.second_hand_auction_system.dtos.responses.sellerInformation.SellerInformationResponse;
import com.second_hand_auction_system.service.sellerInformation.ISellerInformationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/seller-information")
@RequiredArgsConstructor
public class SellerInformationController {

    private final ISellerInformationService sellerInformationService;

    @PostMapping
    public ResponseEntity<SellerInformationResponse> createSellerInformation(
            @RequestBody SellerInformationDto sellerInformationDto) {
        try {
            SellerInformationResponse response = sellerInformationService.createSellerInformation(
                    sellerInformationDto, sellerInformationDto.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PutMapping("/{sellerId}")
    public ResponseEntity<SellerInformationResponse> updateSellerInformation(
            @PathVariable Integer sellerId,
            @RequestBody SellerInformationDto sellerInformationDto) {
        try {
            SellerInformationResponse response = sellerInformationService.updateSellerInformation(sellerId, sellerInformationDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerInformationResponse> getSellerInformationById(
            @PathVariable Integer sellerId) {
        try {
            SellerInformationResponse response = sellerInformationService.getSellerInformationById(sellerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/{sellerId}")
    public ResponseEntity<Void> deleteSellerInformation(
            @PathVariable Integer sellerId) {
        try {
            sellerInformationService.deleteSellerInformation(sellerId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}