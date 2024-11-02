package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface WalletCustomerRepository extends JpaRepository<Wallet, Integer> {
    Optional<Wallet> findByWalletCustomerId(int id);
    Optional<Wallet> findWalletCustomerByUser_Id(int id);

    @Query("SELECT wc FROM Wallet wc WHERE wc.user.id = :userId AND wc.balance >= 100")
    Wallet findByUserIdAndBalanceGreaterThanEqual100(@Param("userId") int userId);

    Optional<Wallet> findByUserId(Integer id);

    @Query("SELECT wc.balance FROM Wallet wc WHERE wc.user.id = :userId")
    Double findBalanceByUserId(@Param("userId") Integer userId);

}
