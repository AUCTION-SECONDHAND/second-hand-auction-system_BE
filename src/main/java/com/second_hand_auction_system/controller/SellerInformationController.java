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

    @PutMapping()
    public ResponseEntity<SellerInformationResponse> updateSellerInformation(

            @RequestBody SellerInformationDto sellerInformationDto) {
        try {
            SellerInformationResponse response = sellerInformationService.updateSellerInformation(sellerInformationDto);
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


    @GetMapping("/user/{userId}")
    public ResponseEntity<SellerInformationResponse> getSellerInformationByUserId(
            @PathVariable Integer userId) {
        try {
            SellerInformationResponse response = sellerInformationService.getSellerInformationByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }


    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<?> getSellerInformationByAuctionId(
            @PathVariable Integer auctionId) {
        try {
            SellerInformationResponse response = sellerInformationService.getSellerInformationByAuctionId(auctionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/top5-seller")
    public ResponseEntity<?> top5Seller() {
        return sellerInformationService.findTop5();
    }


    @GetMapping("")
    public ResponseEntity<SellerInformationResponse> getSellerInformationByToken() {
        try {
            SellerInformationResponse response = sellerInformationService.getSellerInformationByToken();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}