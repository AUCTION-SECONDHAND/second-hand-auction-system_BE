package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.service.transactionWallet.TransactionWalletService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactionWallet")
public class TransactionWalletController {
    private final TransactionWalletService transactionWalletService;

    @GetMapping("/get-transaction-wallet")
    public ResponseEntity<?> getTransactionWallet(
            @RequestParam int size,
            @RequestParam(value = "page") int page,
            @RequestParam(value = "keyword", required = false) String keyword) {
       return transactionWalletService.getAll(size,page);
    }
}
