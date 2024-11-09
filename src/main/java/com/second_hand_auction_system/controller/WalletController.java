package com.second_hand_auction_system.controller;

import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.service.walletCustomer.IWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/walletCustomer")
@RequiredArgsConstructor
public class WalletController {
    private final IWalletService walletService;

    @PostMapping("/deposit")
    public ResponseEntity<ResponseObject> deposit (@RequestBody Deposit deposit) {
        return walletService.depositWallet(deposit);
    }

//    @PostMapping("/withdraw-by-seller")

    @GetMapping("/get-balance")
    public ResponseEntity<ResponseObject> getBalance () {
        return walletService.getWalletCustomerBalance();
    }
//    @GetMapping("/{id}")
//    public ResponseEntity<ResponseObject> getWalletCustomer(@PathVariable Long id) {
//        return walletCustomerService.getWalletCustomer(id);
//    }






}
