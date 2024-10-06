package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.TransactionSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionSystemRepository extends JpaRepository<TransactionSystem, Integer> {
//    Optional<TransactionSystem> findTransactionSystemByT
}
