package com.second_hand_auction_system.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.second_hand_auction_system.dtos.request.walletCustomer.Deposit;
import com.second_hand_auction_system.dtos.request.walletCustomer.PaymentRequest;
import com.second_hand_auction_system.dtos.responses.ResponseObject;
import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.models.User;
import com.second_hand_auction_system.models.WalletCustomer;
import com.second_hand_auction_system.repositories.TransactionWalletRepository;
import com.second_hand_auction_system.repositories.UserRepository;
import com.second_hand_auction_system.repositories.WalletCustomerRepository;
import com.second_hand_auction_system.service.walletCustomer.IWalletCustomerService;
import com.second_hand_auction_system.service.walletCustomer.WalletCustomerService;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.type.Webhook;
import vn.payos.type.WebhookData;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/walletCustomer")
@RequiredArgsConstructor
public class WalletCustomerController {
    private final IWalletCustomerService walletCustomerService;

    @PostMapping("/deposit")
    public ResponseEntity<ResponseObject> deposit (@RequestBody Deposit deposit) {
        return walletCustomerService.depositWallet(deposit);
    }



    @GetMapping("/get-balance")
    public ResponseEntity<ResponseObject> getBalance () {
        return walletCustomerService.getWalletCustomerBalance();
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getWalletCustomer(@PathVariable Long id) {
        return walletCustomerService.getWalletCustomer(id);
    }






}
