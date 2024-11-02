package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.service.transactionWallet.TransactionWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactionWallet")
public class TransactionWalletController {
    private final TransactionWalletService transactionWalletService;



//    @GetMapping("/get-transaction")
//    public ResponseEntity<?> getTransactions(@RequestParam(value = "size", defaultValue = "10") int size,
//                                             @RequestParam(value = "page", defaultValue = "0") int page) {
//        return transactionWalletService.getTransactionWallets(size, page);
//    }

    @GetMapping("/get-transaction-wallet")
    public ResponseEntity<?> getTransactionBider(@RequestParam(value = "limit", defaultValue = "10") int size,
                                             @RequestParam(value = "page", defaultValue = "0") int page) {
        return transactionWalletService.getTransactionWalletsBider(size, page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable("id") int id) {
        return transactionWalletService.getTransactionById(id);
    }




}
