package com.second_hand_auction_system.repositories;

import com.second_hand_auction_system.models.AuctionRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRegistrationsRepository extends JpaRepository<AuctionRegistration, Integer> {
}
