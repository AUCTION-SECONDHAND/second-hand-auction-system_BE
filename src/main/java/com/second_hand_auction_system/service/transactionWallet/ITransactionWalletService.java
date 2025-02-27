package com.second_hand_auction_system.service.transactionWallet;

import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public interface ITransactionWalletService {
    ResponseEntity<?> getAll(String keyword, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

//    ResponseEntity<?> getTransactionWallets(int size, int page);

    ResponseEntity<?> getTransactionById(int id);

    ResponseEntity<?> getTransactionWalletsBider(int size, int page);

    ResponseEntity<?> updateTransaction(Integer transactionId,String vnpTransactionStatus, String vnpTransactionNo);

    ResponseEntity<?> getAllTransaction(int limit, int page, Role role, TransactionType transactionType);

    ResponseEntity<?> upload(String imageUrl,Integer transactionId);

    ResponseEntity<?> getTransaction(Integer auctionId);
}
