package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.service.transactionWallet.TransactionWalletService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transactionWallet")
public class TransactionWalletController {
    private final TransactionWalletService transactionWalletService;

//    @GetMapping("/get-transaction-wallet")
//    public ResponseEntity<?> getTransactionWallet(
//            @RequestParam(value = "size", defaultValue = "10") int size,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "sortBy", defaultValue = "createAt") String sortBy,
//            @RequestParam(value = "keyword", required = false) String keyword,
//            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) { // Thay đổi kiểu dữ liệu sang LocalDate
//        LocalDateTime startDate;
//        LocalDateTime endDate;
//        if (date != null) {
//            startDate = date.atStartOfDay();
//            endDate = date.atTime(23, 59, 59);
//        } else {
//            startDate = LocalDateTime.MIN;
//            endDate = LocalDateTime.now();
//        }
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
//        return transactionWalletService.getAll(keyword, startDate, endDate, pageable);
//    }

    @GetMapping("/get-transaction")
    public ResponseEntity<?> getTransactions(@RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "page", defaultValue = "0") int page) {
        return transactionWalletService.getTransactionWallets(size, page);
    }

    @GetMapping("/get-transaction-wallet")
    public ResponseEntity<?> getTransactionBider(@RequestParam(value = "size", defaultValue = "10") int size,
                                             @RequestParam(value = "page", defaultValue = "0") int page) {
        return transactionWalletService.getTransactionWalletsBider(size, page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable("id") int id) {
        return transactionWalletService.getTransactionById(id);
    }


}
