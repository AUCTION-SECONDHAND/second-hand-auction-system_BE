package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.service.transactionWallet.TransactionWalletService;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionType;
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

    @GetMapping("/get-transaction-admin")
    public ResponseEntity<?> getTransactionAdmin(@RequestParam(value = "limit", defaultValue = "10") int limit,
                                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                                 @RequestParam(value = "role", required = false) Role role,
                                                 @RequestParam(value = "transactionType", required = false) TransactionType transactionType) {
        return transactionWalletService.getAllTransaction(limit, page, role, transactionType);
    }







}
