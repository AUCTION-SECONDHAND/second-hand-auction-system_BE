package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findTransactionWalletByWallet_User_Id(Integer id, Pageable pageable);

    Page<Transaction> findByWallet_User_RoleAndTransactionType(Role role, TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByWallet_User_Role(Role role, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);
}
