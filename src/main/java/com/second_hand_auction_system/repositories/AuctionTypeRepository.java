package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionTypeRepository extends JpaRepository<AuctionType, Integer> {
    boolean existsByAuctionTypeName(String name);
}
