package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Auction;
import com.second_hand_auction_system.models.Order;
import com.second_hand_auction_system.models.Transaction;
import com.second_hand_auction_system.models.Wallet;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findTransactionWalletByWallet_User_Id(Integer id, Pageable pageable);

    Page<Transaction> findByWallet_User_RoleAndTransactionType(Role role, TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByWallet_User_Role(Role role, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    Optional<Transaction> findByWallet_User_Id(Integer id);

    Optional<Transaction> findTransactionByOrder_OrderId(Integer id);

    Optional<Object> findTransactionByOrder(Order order);

    Optional<Transaction> findByWalletAndTransactionTypeAndTransactionStatus(Wallet userWallet, TransactionType transactionType, TransactionStatus transactionStatus);

}
