package com.second_hand_auction_system.service.transactionSystem;

import org.springframework.http.ResponseEntity;

public interface ITransactionSystemSerivce {
    ResponseEntity<?> get(int id);
}
