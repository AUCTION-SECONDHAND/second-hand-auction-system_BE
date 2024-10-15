package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.TransactionWallet;
import com.second_hand_auction_system.utils.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionWalletRepository extends JpaRepository<TransactionWallet, Integer> {
    Optional<TransactionWallet> findTransactionWalletByTransactionWalletCode(long id);

    Page<TransactionWallet> findByWalletCustomer_User_FullNameContainsIgnoreCase(String name, Pageable pageable);

    List<TransactionWallet> findByWalletCustomer_User_FullNameContainingAndCreateAtBetween(String keyword,LocalDateTime start, LocalDateTime end,Pageable pageable);

    List<TransactionWallet> findTransactionWalletByTransactionStatus(TransactionStatus transactionStatus);

    List<TransactionWallet> findTransactionWalletByCreateAt(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}

