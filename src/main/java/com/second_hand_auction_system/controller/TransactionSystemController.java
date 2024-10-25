package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.service.transactionSystem.TransactionSystemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactionSystem")
@RequiredArgsConstructor
public class TransactionSystemController {
    private final TransactionSystemService transactionSystemService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable("id") int id) {
        return transactionSystemService.get(id);
    }
}
