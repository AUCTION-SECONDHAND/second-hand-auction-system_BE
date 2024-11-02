package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
