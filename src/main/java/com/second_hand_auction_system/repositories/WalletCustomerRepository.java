package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.WalletCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletCustomerRepository extends JpaRepository<WalletCustomer, Integer> {
    Optional<WalletCustomer> findByWalletCustomerId(int id);
}
