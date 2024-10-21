package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.WalletSystem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletSystemRepository extends JpaRepository<WalletSystem, Integer> {
}
