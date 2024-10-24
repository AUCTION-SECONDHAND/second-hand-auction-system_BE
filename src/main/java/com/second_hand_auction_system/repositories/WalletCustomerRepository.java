package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.WalletCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface WalletCustomerRepository extends JpaRepository<WalletCustomer, Integer> {
    Optional<WalletCustomer> findByWalletCustomerId(int id);
    Optional<WalletCustomer> findWalletCustomerByUser_Id(int id);

    @Query("SELECT wc FROM WalletCustomer wc WHERE wc.user.id = :userId AND wc.balance >= 100")
    WalletCustomer findByUserIdAndBalanceGreaterThanEqual100(@Param("userId") int userId);

    Optional<WalletCustomer> findByUserId(Integer id);

//    Optional<WalletCustomer> findBy
}
