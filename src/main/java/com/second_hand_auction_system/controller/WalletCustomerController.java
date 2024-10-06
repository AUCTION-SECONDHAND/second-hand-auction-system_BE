package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.request.walletCustomer.PaymentRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.service.walletCustomer.WalletCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/walletCustomer")
@RequiredArgsConstructor
public class WalletCustomerController {
    private final WalletCustomerService walletCustomerService;
    @PostMapping("/deposit")
    public ResponseEntity<ResponseObject> deposit (@RequestBody Deposit deposit) {
        return walletCustomerService.depositWallet(deposit);
    }

    @PostMapping("/callback")
    public ResponseEntity<?> updateStatus(@RequestBody PaymentRequest payment){
        return walletCustomerService.updateStatus(payment);

    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getWalletCustomer(@PathVariable Long id) {
        return walletCustomerService.getWalletCustomer(id);
    }

}
