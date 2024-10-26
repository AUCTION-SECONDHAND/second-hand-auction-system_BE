package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.WalletSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletSystemRepository extends JpaRepository<WalletSystem, Integer> {
    Optional<WalletSystem> findFirstByOrderByWalletAdminIdAsc();

    default WalletSystem getAdminWallet() {
        return findById(1).orElseThrow(() -> new RuntimeException("Admin wallet not found"));
    }
}
