package com.second_hand_auction_system.service.transactionWallet;

import org.springframework.http.ResponseEntity;

public interface ITransactionWalletService {
    ResponseEntity<?> getAll(int size, int page);
}
