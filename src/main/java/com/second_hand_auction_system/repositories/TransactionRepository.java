package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.*;
import com.second_hand_auction_system.utils.Role;
import com.second_hand_auction_system.utils.TransactionStatus;
import com.second_hand_auction_system.utils.TransactionType;
import com.second_hand_auction_system.utils.WalletType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    Page<Transaction> findTransactionWalletByWallet_User_Id(Integer id, Pageable pageable);

    Page<Transaction> findByWallet_User_RoleAndTransactionType(Role role, TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByWallet_User_Role(Role role, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    Optional<Transaction> findByWallet_User_Id(Integer id);
    boolean existsByOrderAndDescription(Order order, String description);

    Optional<Transaction> findTransactionByOrder_OrderId(Integer id);

    Optional<Object> findTransactionByOrder(Order order);

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.order o " +
            "JOIN o.auction a " +
            "WHERE t.wallet = :userWallet " +
            "AND t.transactionType = :transactionType " +
            "AND t.transactionStatus = :transactionStatus " +
            "AND a = :auction")
    Optional<Transaction> findByWalletAndTransactionTypeAndTransactionStatusAndAuction(
            @Param("userWallet") Wallet userWallet,
            @Param("transactionType") TransactionType transactionType,
            @Param("transactionStatus") TransactionStatus transactionStatus,
            @Param("auction") Auction auction);

    boolean existsByOrderAndTransactionStatus(Order winningOrder, TransactionStatus transactionStatus);

    boolean existsByWallet_UserAndOrder_AuctionAndTransactionTypeAndTransactionStatus(
            User user,
            Auction auction,
            TransactionType transactionType,
            TransactionStatus transactionStatus
    );


    boolean existsByTransactionWalletCode(long transactionCode);

    Optional<Transaction> findByOrderAndTransactionType(Order order, TransactionType transactionType);

    @Query("SELECT t FROM Transaction t " +
            "JOIN t.wallet w " +
            "JOIN Auction a ON a.wallet.walletId = w.walletId " +
            "WHERE a.auctionId = :auctionId " +
            "AND w.walletType = :walletType")
    List<Transaction> findTransactionsByAuctionId(@Param("walletType") WalletType walletType, @Param("auctionId") Integer auctionId);


    List<Transaction> findTransactionByWallet_WalletId(Integer walletId);





}
