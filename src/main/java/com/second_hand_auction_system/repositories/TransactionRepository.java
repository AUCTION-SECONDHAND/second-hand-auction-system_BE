package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findTransactionWalletByWallet_User_Id(Integer id, Pageable pageable);
}
