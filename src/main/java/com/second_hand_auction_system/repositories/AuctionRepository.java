package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Integer> {
}
